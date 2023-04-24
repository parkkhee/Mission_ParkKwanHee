package com.ll.gramgram.boundedContext.likeablePerson.controller;

import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/usr/likeablePerson")
@RequiredArgsConstructor
public class LikeablePersonController {
    private final Rq rq;
    private final LikeablePersonService likeablePersonService;
    private final InstaMemberService instaMemberService;


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/like")
    public String showLike() {
        return "usr/likeablePerson/like";
    }

    @AllArgsConstructor
    @Getter
    public static class LikeForm {
        private final String username;
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/like")
    public String like(@Valid LikeForm likeForm) {

        // LikeablePerson에 추가 할 수 있는지 확인
        RsData canLikeRsData = likeablePersonService.canLike(rq.getMember(),
                likeForm.getUsername());

        if (canLikeRsData.isFail()) {
            return rq.historyBack(canLikeRsData);
//            return "usr/home/test"; // 테스트 코드 실행을 위해
        }


        // 중복 되는 LikeablePerson 이 있는지 확인
        RsData isAlreadyLiked = likeablePersonService.isAlreadyLiked(rq.getMember(),
                likeForm.getUsername(), likeForm.getAttractiveTypeCode());
        if (isAlreadyLiked.isFail()) {
            return rq.historyBack(canLikeRsData);
        }
        // 단순 매력포인트 변경이라면 업데이트 후 통과
        if (isAlreadyLiked.getResultCode().equals("S-2")) {
            return rq.redirectWithMsg("/usr/likeablePerson/list", isAlreadyLiked);
        }


        RsData<LikeablePerson> createRsData = likeablePersonService.like(rq.getMember(),
                likeForm.getUsername(), likeForm.getAttractiveTypeCode());

        return rq.redirectWithMsg("/usr/likeablePerson/list", createRsData);
//        return "usr/home/test";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public String showList(Model model) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            List<LikeablePerson> likeablePeople = instaMember.getFromLikeablePeople();
            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/list";
    }

    @PreAuthorize("isAuthenticated()") //먼저 웹 사이트에 로그인이 되어 있는지 확인.
    @DeleteMapping("/{id}")
    public String cancel(@PathVariable("id") Long id) {
        LikeablePerson likeablePerson = likeablePersonService.likeablepersonbyId(id).orElse(null);

        RsData canDeleteRsData = likeablePersonService.canCancel(rq.getMember(), likeablePerson);

        if (canDeleteRsData.isFail()) return rq.historyBack(canDeleteRsData);

        RsData<LikeablePerson> deleteRsdata = likeablePersonService.cancel(likeablePerson);

        if (deleteRsdata.isFail()) {
            return rq.historyBack(deleteRsdata);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", deleteRsdata);


    }

    //호감사유변경
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String showModify(@PathVariable Long id, Model model) {
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElseThrow();

        RsData canModifyRsData = likeablePersonService.canModifyLike(rq.getMember(), likeablePerson);

        if (canModifyRsData.isFail()) return rq.historyBack(canModifyRsData);

        model.addAttribute("likeablePerson", likeablePerson);

        return "usr/likeablePerson/modify";
    }

    @AllArgsConstructor
    @Getter
    public static class ModifyForm {
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(@PathVariable Long id, @Valid ModifyForm modifyForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.modifyLike(rq.getMember(), id, modifyForm.getAttractiveTypeCode());

        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
    }
}
