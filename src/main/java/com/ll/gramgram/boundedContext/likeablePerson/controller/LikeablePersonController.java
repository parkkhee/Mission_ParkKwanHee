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
@RequestMapping("/likeablePerson")
@RequiredArgsConstructor
public class LikeablePersonController {
    private final Rq rq;
    private final LikeablePersonService likeablePersonService;
    private final InstaMemberService instaMemberService;


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/add")
    public String showAdd() {
        return "usr/likeablePerson/add";
    }

    @AllArgsConstructor
    @Getter
    public static class AddForm {
        private final String username;
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add")
    public String add(@Valid AddForm addForm) {

        // LikeablePerson에 추가 할 수 있는지 확인
        RsData canLikeRsData = likeablePersonService.canLike(rq.getMember(),
                addForm.getUsername());

        if (canLikeRsData.isFail()) {
            return rq.historyBack(canLikeRsData);
//            return "usr/home/test"; // 테스트 코드 실행을 위해
        }


        // 중복 되는 LikeablePerson 이 있는지 확인
        RsData isAlreadyLiked = likeablePersonService.isAlreadyLiked(rq.getMember(),
                addForm.getUsername(), addForm.getAttractiveTypeCode());
        if (isAlreadyLiked.isFail()) {
            return rq.historyBack(canLikeRsData);
        }
        // 단순 매력포인트 변경이라면 업데이트 후 통과
        if (isAlreadyLiked.getResultCode().equals("S-2")) {
            return rq.redirectWithMsg("/likeablePerson/list", isAlreadyLiked);
        }


        RsData<LikeablePerson> createRsData = likeablePersonService.like(rq.getMember(),
                addForm.getUsername(), addForm.getAttractiveTypeCode());

        return rq.redirectWithMsg("/likeablePerson/list", createRsData);
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
    public String delete(@PathVariable("id") Long id) {
        LikeablePerson likeablePerson = likeablePersonService.likeablepersonbyId(id).orElse(null);

        RsData canActorDeleteRsData = likeablePersonService.canActorDelete(rq.getMember(), likeablePerson);

        if (canActorDeleteRsData.isFail()) return rq.historyBack(canActorDeleteRsData);

        RsData<LikeablePerson> deleteRsdata = likeablePersonService.delete(likeablePerson);

        if (deleteRsdata.isFail()) {
            return rq.historyBack(deleteRsdata);
        }

        return rq.redirectWithMsg("/likeablePerson/list", deleteRsdata);


    }


}
