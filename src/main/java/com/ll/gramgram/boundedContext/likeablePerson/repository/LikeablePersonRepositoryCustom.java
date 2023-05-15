package com.ll.gramgram.boundedContext.likeablePerson.repository;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;

import java.util.List;
import java.util.Optional;

public interface LikeablePersonRepositoryCustom {
    Optional<LikeablePerson> findQslByFromInstaMemberIdAndToInstaMember_username(long fromInstaMemberId, String toInstaMemberUsername);

    List<LikeablePerson> findAll(BooleanBuilder builder, OrderSpecifier<?> orderSpecifier);

    List<LikeablePerson> findAllByCreateDate(BooleanBuilder builder, OrderSpecifier<?> orderSpecifier);

    List<LikeablePerson> findQslByToInstaMemberAndGenderAndAttractiveTypeCode(InstaMember instaMember, String gender, int attractiveTypeCode, int sortCode);

}
