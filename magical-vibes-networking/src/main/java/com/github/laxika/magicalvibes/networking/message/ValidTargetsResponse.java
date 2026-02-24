package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;
import java.util.UUID;

public record ValidTargetsResponse(
        MessageType type,
        List<UUID> validPermanentIds,
        List<UUID> validPlayerIds,
        int minTargets,
        int maxTargets,
        String prompt
) {
    public ValidTargetsResponse(List<UUID> validPermanentIds, List<UUID> validPlayerIds, int minTargets, int maxTargets, String prompt) {
        this(MessageType.VALID_TARGETS_RESPONSE, validPermanentIds, validPlayerIds, minTargets, maxTargets, prompt);
    }
}
