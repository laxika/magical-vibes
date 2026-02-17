package com.github.laxika.magicalvibes.networking.message;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlayCardRequest(int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments,
                              List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, Boolean fromGraveyard) {
}
