package com.ll.gramgram.base.initData;

import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "test"})
public class NotProd {
    @Bean
    CommandLineRunner initData(
            MemberService memberService,
            InstaMemberService instaMemberService,
            LikeablePersonService likeablePersonService
    ) {
        return args -> {
            Member memberAdmin = memberService.join("admin", "aaaa@aaaa.com", "1234").getData();
            Member memberUser1 = memberService.join("user1", "aaaa@aaaa.com","1234").getData();
            Member memberUser2 = memberService.join("user2", "aaaa@aaaa.com","1234").getData();
            Member memberUser3 = memberService.join("user3", "aaaa@aaaa.com","1234").getData();
            Member memberUser4 = memberService.join("user4", "aaaa@aaaa.com","1234").getData();

            Member memberUser5ByKakao = memberService.whenSocialLogin("KAKAO", "KAKAO__2733209617").getData();
            Member memberUser5ByGoogle = memberService.whenSocialLogin("GOOGLE", "GOOGLE__118170531600507981019").getData();
            Member memberUser5ByNaver = memberService.whenSocialLogin("NAVER", "NAVER__6XwrFO3rH1YF4kOYRRMC8KCdKzIiStopYJ34i3cbMlU").getData();

            instaMemberService.connect(memberUser2, "insta_user2", "M");
            instaMemberService.connect(memberUser3, "insta_user3", "W");
            instaMemberService.connect(memberUser4, "insta_user4", "M");

            likeablePersonService.like(memberUser3, "insta_user4", 1);
            likeablePersonService.like(memberUser3, "insta_user100", 2);
        };
    }
}
