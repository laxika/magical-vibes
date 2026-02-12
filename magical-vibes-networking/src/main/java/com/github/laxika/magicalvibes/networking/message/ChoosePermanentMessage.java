package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;
import java.util.UUID;

public record ChoosePermanentMessage(MessageType type, List<UUID> permanentIds, String prompt) {

    public ChoosePermanentMessage(List<UUID> permanentIds, String prompt) {
        this(MessageType.CHOOSE_PERMANENT, permanentIds, prompt);
    }
}
