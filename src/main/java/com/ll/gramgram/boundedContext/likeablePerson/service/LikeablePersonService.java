package com.ll.gramgram.boundedContext.likeablePerson.service;

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

    @Value("${custom.registration.limit}")
    private int registrationLimit;

    // 호감표시가 가능한지 체크하는 메서드
    public RsData canLike(Member member, String username) {
        if ( member.hasConnectedInstaMember() == false ) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (member.getInstaMember().getUsername().equals(username.trim())) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }

        //로그인한 유저가 좋아하는 LikeablePerson ListArray 불러오기
        List<LikeablePerson> memberbyFromInstaMemberId = likeablePersonRepository.findByFromInstaMemberId(
                member.getInstaMember().getId());
        // 호감상대 10명으로 제한
        if (memberbyFromInstaMemberId.size()>=registrationLimit) {
            return RsData.of("F-4","이미 호감상대가 10명입니다;(");
        }
        return RsData.of("S-1", "이미 있는 유저 입니다. 다른 매력 포인트를 선택해 주세요.");
    }

    public RsData isAlreadyLiked(Member member, String username, int attractiveTypeCode) {
        //호감 추가 하려는 유저에 대한 InstaMember 를 가져온다
        Optional<InstaMember> toInstabyUsername = instaMemberService.findByUsername(username.trim());

        if (toInstabyUsername.isPresent()) {
            // 해당하는 LikeablePerson 있는지 확인해보기
            Optional<LikeablePerson> targetLikeablePerson =
                    likeablePersonRepository.findByFromInstaMemberIdAndToInstaMemberId(
                            member.getInstaMember().getId(), toInstabyUsername.get().getId());
            //동일한 유저 인지 확인하는 메서드
            if ( targetLikeablePerson.isPresent() ) {

                if (Objects.equals(targetLikeablePerson
                        .get().getAttractiveTypeCode(), attractiveTypeCode)) {
                    return RsData.of("F-3","이미 동일한 유형으로 등록한 호감상대 입니다.");
                }

                // 이미 있는 LikeablePerson 이지만 호감 유형이 다를때 수정을 위해 실행
                isAlreadyLikedModify(targetLikeablePerson.get(),attractiveTypeCode);

                return RsData.of("S-2","호감 상대 %s 유형이 변경 되었습니다.".formatted(username));
            }
        }
        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록가능합니다.".formatted(username));
    }

    @Transactional
    public void isAlreadyLikedModify(LikeablePerson targetLikeablePerson, int attractiveTypeCode) {

        // 수정할 LikeablePerson 찾기
        LikeablePerson byFromInstaMemberIdAndToInstaMemberId =
                targetLikeablePerson;

        // 레포지토리에서 수정하기
        likeablePersonRepository.updateByLikeablePersonId(
                byFromInstaMemberIdAndToInstaMemberId.getId(), attractiveTypeCode);

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

    public RsData<LikeablePerson> canActorDelete(Member member, LikeablePerson likeablePerson) {

        if (likeablePerson == null) {
            return RsData.of("F-1", "존재하지 않는 회원입니다.");
        }

        if (!member.getInstaMember().getId().equals(likeablePerson.getFromInstaMember().getId())) {
            return RsData.of("F-2", "권한이 없는 회원입니다.");
        }

        return RsData.of("S-1", "권한이 있는 회원입니다.");

    }

    @Transactional
    public RsData<LikeablePerson> delete(LikeablePerson likeablePerson) {

        likeablePersonRepository.delete(likeablePerson);

        String likeCanceledUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(likeCanceledUsername));

    }

    public Optional<LikeablePerson> likeablepersonbyId(Long id) {

        Optional<LikeablePerson> likeablePerson = likeablePersonRepository.findById(id);

        return likeablePerson;

    }
}
