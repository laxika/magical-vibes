package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.MessageType;

public record GameMessage(MessageType type, GameResponse game) {
}
