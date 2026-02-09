package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record JoinGameMessage(MessageType type, JoinGame game) {
}
