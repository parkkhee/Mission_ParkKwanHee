package com.ll.gramgram.boundedContext.likeablePerson.controller;


import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class LikeablePersonControllerTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private LikeablePersonService likeablePersonService;

    @Test
    @DisplayName("등록 폼(인스타 인증을 안해서 폼 대신 메세지)")
    @WithUserDetails("user1")
    void t001() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/likeablePerson/add"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("showAdd"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        먼저 본인의 인스타그램 아이디를 입력해주세요.
                        """.stripIndent().trim())))
        ;
    }

    @Test
    @DisplayName("등록 폼")
    @WithUserDetails("user2")
    void t002() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/likeablePerson/add"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("showAdd"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        <input type="text" name="username"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="radio" name="attractiveTypeCode" value="1"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="radio" name="attractiveTypeCode" value="2"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="radio" name="attractiveTypeCode" value="3"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="submit" value="추가"
                        """.stripIndent().trim())));
        ;
    }

    @Test
    @DisplayName("등록 폼 처리(user2가 user3에게 호감표시(외모))")
    @WithUserDetails("user2")
    void t003() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/likeablePerson/add")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "insta_user3")
                        .param("attractiveTypeCode", "1")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("add"))
                .andExpect(status().is3xxRedirection());
        ;
    }

    @Test
    @DisplayName("등록 폼 처리(user2가 abcd에게 호감표시(외모), abcd는 아직 우리 서비스에 가입하지 않은상태)")
    @WithUserDetails("user2")
    void t004() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/likeablePerson/add")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "abcd")
                        .param("attractiveTypeCode", "2")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("add"))
                .andExpect(status().is3xxRedirection());
        ;
    }

    @Test
    @DisplayName("호감목록")
    @WithUserDetails("user3")
    void t005() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/likeablePerson/list"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("showList"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        <span class="toInstaMember_username">insta_user4</span>
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <span class="toInstaMember_attractiveTypeDisplayName">외모</span>
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <span class="toInstaMember_username">insta_user100</span>
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <span class="toInstaMember_attractiveTypeDisplayName">성격</span>
                        """.stripIndent().trim())));
        ;
    }

//    @Autowired  //기능만 테스트
//    LikeablePersonController likeablePersonController;
//    @Test
//    @DisplayName("로그인한 유저가 삭제하려는 인스타 아이디를 잘 가져오는지 테스트")
//    @WithUserDetails("user3")
//    void t006() throws Exception {
//        // WHEN
//        Long likeablePerson = likeablePersonController.delete(1);
//
//        // THEN
//        Assertions.assertThat(likeablePerson).isEqualTo(1L);
//    }


    @Test
    @DisplayName("호감삭제(없는거 삭제, 삭제가 안되어야 함)")
    @WithUserDetails("user3")
    void t007() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(
                        delete("/likeablePerson/100")
                                .with(csrf())
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().is4xxClientError())
        ;
    }

    @Test
    @DisplayName("호감삭제(권한이 없는 경우, 삭제가 안됨)")
    @WithUserDetails("user2")
    void t008() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(
                        delete("/likeablePerson/1")
                                .with(csrf())
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().is4xxClientError())
        ;

        assertThat(likeablePersonService.likeablepersonbyId(1L).isPresent()).isEqualTo(true);
    }

    @Test
    @DisplayName("케이스 4 : 한명의 인스타회원이 다른 인스타회원에게 중복으로 호감표시를 할 수 없습니다.")
    @WithUserDetails("user3")
    void t009() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(
                        post("/likeablePerson/add")
                                .with(csrf())
                                .param("username", "insta_user100")
                                .param("attractiveTypeCode", "2")
                )
                .andDo(print());


        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("add"))
                .andExpect(content().string("fail"))
        ;

    }

    @Autowired
    MemberService memberService;
    @Autowired
    InstaMemberService instaMemberService;
    @Test
    @DisplayName("케이스 5 : 한명의 인스타회원이 11명 이상의 호감상대를 등록 할 수 없습니다.")
    @WithUserDetails("user3")
    void t010() throws Exception {

        // WHEN
        Member memberUser3 = memberService.findByUsername("user3").orElseThrow();
//        instaMemberService.connect(memberUser3, "insta_user3", "W");
        for (int i = 0; i < 8; i++) {
            likeablePersonService.like(memberUser3, "insta_use"+i, 1);
        }

        ResultActions resultActions = mvc

                .perform(
                        post("/likeablePerson/add")
                                .with(csrf())
                                .param("username", "qqqq")
                                .param("attractiveTypeCode", "2")
                )
                .andDo(print());


        // THEN
//        System.out.println("홓호홓 : " + likeablePersonService.findByFromInstaMemberId(memberUser3.getInstaMember().getId()).size());
        assertThat(likeablePersonService.findByFromInstaMemberId(memberUser3.getInstaMember().getId()).size()).isEqualTo(10);
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("add"))
                .andExpect(content().string("fail"))
        ;

    }

    @Test
    @DisplayName("케이스 6 : 케이스 4 가 발생했을 때 기존의 사유와 다른 사유로 호감을 표시하는 경우에는 성공으로 처리한다.")
    @WithUserDetails("user3")
    void t011() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(
                        post("/likeablePerson/add")
                                .with(csrf())
                                .param("username", "insta_user100")
                                .param("attractiveTypeCode", "1")
                )
                .andDo(print());


        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("add"))
                .andExpect(content().string("fail"))
        ;

    }



}
