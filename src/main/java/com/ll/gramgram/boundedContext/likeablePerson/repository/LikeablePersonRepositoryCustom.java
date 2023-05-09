package com.ll.gramgram.boundedContext.likeablePerson.repository;

import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;

import java.util.List;
import java.util.Optional;

public interface LikeablePersonRepositoryCustom {
    Optional<LikeablePerson> findQslByFromInstaMemberIdAndToInstaMember_username(long fromInstaMemberId, String toInstaMemberUsername);

    Optional<LikeablePerson> findQslByToInstaMemberIdAndToInstaMember_gender(long toInstaMemberId, String gender);

    List<LikeablePerson> findAll(BooleanBuilder builder, OrderSpecifier<?> orderSpecifier);
}
