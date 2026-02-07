package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.MessageType;

public record LobbyGameMessage(MessageType type, LobbyGame game) {
}
