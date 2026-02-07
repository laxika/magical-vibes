package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

@Getter
@RequiredArgsConstructor
public class Player {

    private final Long id;
    private final String username;
    private final WebSocketSession session;
}
