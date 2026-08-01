package com.github.laxika.magicalvibes.networking.message;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlayCardRequest(int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                              List<UUID> targetIds, List<UUID> convokeCreatureIds, Boolean fromGraveyard,
                              UUID sacrificePermanentId, Integer phyrexianLifeCount, UUID fromExileCardId,
                              List<UUID> alternateCostSacrificePermanentIds, Boolean flashback,
                              Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                              Boolean kicked, Boolean fromLibraryTop, String chosenGraveyardType,
                              Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                              List<UUID> imposedSacrificePermanentIds,
                              List<UUID> additionalCostSacrificePermanentIds,
                              List<String> repeatedAdditionalCosts,
                              Boolean buyback) {

    /** Convenience for the overwhelming majority of casts, which pay no repeatable additional cost. */
    public PlayCardRequest(int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                           List<UUID> targetIds, List<UUID> convokeCreatureIds, Boolean fromGraveyard,
                           UUID sacrificePermanentId, Integer phyrexianLifeCount, UUID fromExileCardId,
                           List<UUID> alternateCostSacrificePermanentIds, Boolean flashback,
                           Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                           Boolean kicked, Boolean fromLibraryTop, String chosenGraveyardType,
                           Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                           List<UUID> imposedSacrificePermanentIds,
                           List<UUID> additionalCostSacrificePermanentIds) {
        this(cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard,
                sacrificePermanentId, phyrexianLifeCount, fromExileCardId, alternateCostSacrificePermanentIds,
                flashback, exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, fromLibraryTop,
                chosenGraveyardType, discardHandCardIndex, discardHandCardIndices,
                imposedSacrificePermanentIds, additionalCostSacrificePermanentIds, List.of(), null);
    }
}
