package com.github.laxika.magicalvibes.networking.message;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlayCardRequest(int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                              List<UUID> targetIds, List<UUID> convokeCreatureIds, Boolean fromGraveyard,
                              UUID sacrificePermanentId, Integer phyrexianLifeCount, UUID fromExileCardId,
                              List<UUID> alternateCostSacrificePermanentIds, Boolean flashback,
                              Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                              Boolean kicked, Boolean fromLibraryTop) {
}
