package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.base.event.EventAfterModifyAttractiveType;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<Notification> findByToInstaMember(InstaMember toInstaMember) {
        return notificationRepository.findByToInstaMember(toInstaMember);
    }

    // 호감 표시 알림 생성 메서드
    public Notification createLikeNotification(InstaMember fromInstaMember, InstaMember toInstaMember) {
        Notification notification = Notification.builder()
                .createDate(LocalDateTime.now())
                .modifyDate(LocalDateTime.now())
                .toInstaMember(toInstaMember)
                .fromInstaMember(fromInstaMember)
                .typeCode("Like")
                .build();

        notificationRepository.save(notification);
        return notification;
    }

    @Transactional
    public void updateReadDate(List<Notification> notifications) {

        notifications.stream().filter(e->e.getReadDate() == null).forEach(e -> e.updateReadDate(LocalDateTime.now()));

    }

//    // 알림 전송 메서드
//    public void sendNotification(Notification notification) {
//
//        List<Notification> findToNotification = notificationRepository.findByToInstaMember(notification.getToInstaMember());
//        for (Long id : findToNot) {
//        // notification을 전송하는 코드 구현
//        try {
//            sseEmitter.send(notification, MediaType.APPLICATION_JSON);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
