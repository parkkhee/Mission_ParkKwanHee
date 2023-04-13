package com.ll.gramgram.boundedContext.likeablePerson.repository;

import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeablePersonRepository extends JpaRepository<LikeablePerson, Long> {
    List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId);
    Optional<LikeablePerson> findByFromInstaMemberIdAndToInstaMemberId(Long fromInstaMemberId, Long toInstaMemberId);

    List<LikeablePerson> findByToInstaMember_username(String username); //이런식으로도 호출가능
    LikeablePerson findByFromInstaMemberIdAndToInstaMember_username(long fromInstaMemberId, String username);//강사님 코드

    @Modifying(clearAutomatically = true)
    @Query("update LikeablePerson l set l.attractiveTypeCode = :attractiveTypeCode where l.id = :id")
    void updateByLikeablePersonId(@Param("id")Long id, @Param("attractiveTypeCode")int attractiveTypeCode);
}
