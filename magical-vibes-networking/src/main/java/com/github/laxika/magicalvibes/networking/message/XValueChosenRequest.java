package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record XValueChosenRequest(MessageType type, int chosenValue) {
}
