package com.ll.gramgram.boundedContext.member.service;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberServiceTest {

    @Autowired
    private MemberService memberservice;

    @Test
    @DisplayName("이메일 전송 성공")
    public void sendMailTest() {
        // 이 보내는 주소는 상대방이 확인할 수 없다. 이메일에 찍히는 "보낸 사람"은 로그인한 아이디이다.
        String from = "admin@icia.com";
        String to = "메일 받아 볼 계정";
        String title = "가입 확인 메일입니다. o(*'▽'*)/☆ﾟ’";

        /*
            <p>가입하려면 아래 링크를 클릭하세요.</p>
            <p><a href='http://localhost:8081/member/join/check?checkcode=1234'>클릭하세요.</a></p>
         */


        StringBuilder builder = new StringBuilder("<p>가입하려면 아래 링크를 클릭하세요.</p>")
                .append("<p><a href='http://localhost:8081/member/join/check?checkcode=")
                .append("'>클릭하세요.</a></p>");

        try {
            memberservice.sendMail(from, to, title, builder.toString());
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

}