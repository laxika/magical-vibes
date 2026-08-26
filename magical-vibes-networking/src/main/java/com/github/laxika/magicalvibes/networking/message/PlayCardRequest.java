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
                              Boolean buyback,
                              Integer sharedColorDiscardHandCardIndex,
                              UUID beholdPermanentId,
                              Integer beholdHandCardIndex,
                              List<UUID> beholdPermanentIds,
                              List<Integer> beholdHandCardIndices,
                              List<UUID> exileCounterCostPermanentIds,
                              String beholdCreatureType,
                              Boolean morph,
                              String chosenCreatureType,
                              Boolean foretell,
                              Boolean adventure) {

    public PlayCardRequest(int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
                           List<UUID> targetIds, List<UUID> convokeCreatureIds, Boolean fromGraveyard,
                           UUID sacrificePermanentId, Integer phyrexianLifeCount, UUID fromExileCardId,
                           List<UUID> alternateCostSacrificePermanentIds, Boolean flashback,
                           Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                           Boolean kicked, Boolean fromLibraryTop, String chosenGraveyardType,
                           Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                           List<UUID> imposedSacrificePermanentIds,
                           List<UUID> additionalCostSacrificePermanentIds,
                           List<String> repeatedAdditionalCosts, Boolean buyback,
                           Integer sharedColorDiscardHandCardIndex, UUID beholdPermanentId,
                           Integer beholdHandCardIndex, List<UUID> beholdPermanentIds,
                           List<Integer> beholdHandCardIndices, List<UUID> exileCounterCostPermanentIds,
                           String beholdCreatureType, Boolean morph, String chosenCreatureType) {
        this(cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, fromExileCardId,
                alternateCostSacrificePermanentIds, flashback, exileGraveyardCardIndex,
                exileGraveyardCardIndices, kicked, fromLibraryTop, chosenGraveyardType,
                discardHandCardIndex, discardHandCardIndices, imposedSacrificePermanentIds,
                additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback,
                sharedColorDiscardHandCardIndex, beholdPermanentId, beholdHandCardIndex,
                beholdPermanentIds, beholdHandCardIndices, exileCounterCostPermanentIds,
                beholdCreatureType, morph, chosenCreatureType, null, null);
    }

    public PlayCardRequest(int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments,
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
        this(cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard,
                sacrificePermanentId, phyrexianLifeCount, fromExileCardId, alternateCostSacrificePermanentIds,
                flashback, exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, fromLibraryTop,
                chosenGraveyardType, discardHandCardIndex, discardHandCardIndices,
                imposedSacrificePermanentIds, additionalCostSacrificePermanentIds,
                repeatedAdditionalCosts, buyback, null, null, null, null, null, null, null, null, null, null, null);
    }

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
                imposedSacrificePermanentIds, additionalCostSacrificePermanentIds, List.of(), null, null,
                null, null, null, null, null, null, null, null, null, null);
    }
}
