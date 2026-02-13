package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record MayAbilityChosenRequest(MessageType type, boolean accepted) {
}
