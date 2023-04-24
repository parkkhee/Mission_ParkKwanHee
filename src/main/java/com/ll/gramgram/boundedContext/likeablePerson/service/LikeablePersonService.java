package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;


    // 호감표시가 가능한지 체크하는 메서드
    public RsData canLike(Member actor, String username) {
        if ( actor.hasConnectedInstaMember() == false ) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        InstaMember fromInstaMember = actor.getInstaMember();

        if (fromInstaMember.getUsername().equals(username.trim())) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }
        // 액터가 생성한 `좋아요` 들 가져오기
        List<LikeablePerson> fromLikeablePeople = fromInstaMember.getFromLikeablePeople();

        LikeablePerson fromLikeablePerson = fromLikeablePeople
                .stream()
                .filter(e -> e.getToInstaMember().getUsername().equals(username))
                .findFirst()
                .orElse(null);

        long likeablePersonFromMax = AppConfig.getLikeablePersonFromMax();

        //로그인한 유저가 좋아하는 LikeablePerson ListArray 불러오기
        List<LikeablePerson> memberbyFromInstaMemberId = likeablePersonRepository.findByFromInstaMemberId(
                actor.getInstaMember().getId());
        // 호감상대 10명으로 제한
        if (memberbyFromInstaMemberId.size()>=likeablePersonFromMax) {
            return RsData.of("F-4","이미 호감상대가 %s명입니다;(".formatted(likeablePersonFromMax));
        }
        return RsData.of("S-1", "이미 있는 유저 입니다. 다른 매력 포인트를 선택해 주세요.");
    }

    public RsData isAlreadyLiked(Member member, String username, int attractiveTypeCode) {

        List<LikeablePerson> fromLikeablePeople = member.getInstaMember().getFromLikeablePeople();

        LikeablePerson fromLikeablePerson = fromLikeablePeople
                .stream()
                .filter(e -> e.getToInstaMember().getUsername().equals(username))
                .findFirst()
                .orElse(null);


        if (fromLikeablePerson == null) {
            return RsData.of("F-7", "호감표시를 하지 않았습니다.");
        }

        String oldAttractiveTypeDisplayName = fromLikeablePerson.getAttractiveTypeDisplayName();

        fromLikeablePerson.setAttractiveTypeCode(attractiveTypeCode);
//        likeablePersonRepository.save(fromLikeablePerson);

        String newAttractiveTypeDisplayName = fromLikeablePerson.getAttractiveTypeDisplayName();

        return RsData.of("S-3",
                "%s님에 대한 호감사유를 %s에서 %s(으)로 변경합니다.".formatted(username,
                        oldAttractiveTypeDisplayName, newAttractiveTypeDisplayName));

    }

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {


        InstaMember fromInstaMember = member.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        // 너가 좋아하는 호감표시 생겼어.
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 호감표시 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    public RsData<LikeablePerson> canCancel(Member member, LikeablePerson likeablePerson) {

        if (likeablePerson == null) {
            return RsData.of("F-1", "존재하지 않는 회원입니다.");
        }

        if (!member.getInstaMember().getId().equals(likeablePerson.getFromInstaMember().getId())) {
            return RsData.of("F-2", "권한이 없는 회원입니다.");
        }

        return RsData.of("S-1", "권한이 있는 회원입니다.");

    }

    @Transactional
    public RsData<LikeablePerson> cancel(LikeablePerson likeablePerson) {

        // 너가 생성한 좋아요가 사라졌어.
        likeablePerson.getFromInstaMember().removeFromLikeablePerson(likeablePerson);

        // 너가 받은 좋아요가 사라졌어.
        likeablePerson.getToInstaMember().removeToLikeablePerson(likeablePerson);


        likeablePersonRepository.delete(likeablePerson);

        String likeCanceledUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(likeCanceledUsername));

    }

    public Optional<LikeablePerson> likeablepersonbyId(Long id) {

        Optional<LikeablePerson> likeablePerson = likeablePersonRepository.findById(id);

        return likeablePerson;

    }

    @Transactional
    public RsData<LikeablePerson> modifyLike(Member actor, Long id, int attractiveTypeCode) {
        LikeablePerson likeablePerson = findById(id).orElseThrow();
        RsData canModifyRsData = canModifyLike(actor, likeablePerson);

        if (canModifyRsData.isFail()) {
            return canModifyRsData;
        }

        likeablePerson.setAttractiveTypeCode(attractiveTypeCode);

        return RsData.of("S-1", "호감사유를 수정하였습니다.");
    }

    public RsData canModifyLike(Member actor, LikeablePerson likeablePerson) {
        if (!actor.hasConnectedInstaMember()) {
            return RsData.of("F-1", "먼저 본인의 인스타그램 아이디를 입력해주세요.");
        }

        InstaMember fromInstaMember = actor.getInstaMember();

        if (!Objects.equals(likeablePerson.getFromInstaMember().getId(), fromInstaMember.getId())) {
            return RsData.of("F-2", "해당 호감표시를 취소할 권한이 없습니다.");
        }


        return RsData.of("S-1", "호감표시취소가 가능합니다.");
    }
}
