package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;
import java.util.UUID;

public record ChooseMultiplePermanentsMessage(MessageType type, List<UUID> permanentIds, int maxCount, String prompt) {

    public ChooseMultiplePermanentsMessage(List<UUID> permanentIds, int maxCount, String prompt) {
        this(MessageType.CHOOSE_MULTIPLE_PERMANENTS, permanentIds, maxCount, prompt);
    }
}
