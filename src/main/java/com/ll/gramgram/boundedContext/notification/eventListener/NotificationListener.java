package com.ll.gramgram.boundedContext.notification.eventListener;

import com.ll.gramgram.base.event.EventAfterModifyAttractiveType;
import com.ll.gramgram.base.event.EventNotification;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @EventListener
    @Transactional
    public void listen(EventNotification event) {
        Notification likeNotification = notificationService.createLikeNotification(event.getLikeablePerson().getFromInstaMember(),
                event.getLikeablePerson().getFromInstaMember());

//        notificationService.sendNotification(likeNotification);
    }


}
