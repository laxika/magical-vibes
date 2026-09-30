package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.List;
import java.util.UUID;

/**
 * The spell whose effective cast cost is being computed: the game state, the player
 * casting it, the card itself, and the cast mode, source, and choices relevant to cost modifiers.
 */
public record CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                      boolean flashbackCost, int xValue, boolean plottingFromHand,
                                      Zone sourceZone, boolean castFaceDown,
                                      boolean collectEvidenceCostPaid, boolean kicked,
                                      UUID turnFaceUpPermanentId, boolean blitzCost,
                                      List<UUID> targetIds) {

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                   boolean flashbackCost, int xValue, boolean plottingFromHand,
                                   Zone sourceZone, boolean castFaceDown,
                                   boolean collectEvidenceCostPaid, boolean kicked,
                                   UUID turnFaceUpPermanentId, boolean blitzCost) {
        this(gameData, castingPlayerId, spell, flashbackCost, xValue, plottingFromHand, sourceZone,
                castFaceDown, collectEvidenceCostPaid, kicked, turnFaceUpPermanentId, blitzCost,
                List.of());
    }

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                   boolean flashbackCost, int xValue, boolean plottingFromHand,
                                   Zone sourceZone, boolean castFaceDown,
                                   boolean collectEvidenceCostPaid, boolean kicked) {
        this(gameData, castingPlayerId, spell, flashbackCost, xValue, plottingFromHand, sourceZone,
                castFaceDown, collectEvidenceCostPaid, kicked, null);
    }

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                   boolean flashbackCost, List<UUID> targetIds) {
        this(gameData, castingPlayerId, spell, flashbackCost, 0, false, null, false,
                false, false, null, false, targetIds);
    }

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                   boolean flashbackCost, int xValue, boolean plottingFromHand,
                                   Zone sourceZone, boolean castFaceDown,
                                   boolean collectEvidenceCostPaid, boolean kicked,
                                   UUID turnFaceUpPermanentId) {
        this(gameData, castingPlayerId, spell, flashbackCost, xValue, plottingFromHand, sourceZone,
                castFaceDown, collectEvidenceCostPaid, kicked, turnFaceUpPermanentId, false);
    }

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell) {
        this(gameData, castingPlayerId, spell, false, 0, false, null, false, false, false);
    }

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                   boolean flashbackCost) {
        this(gameData, castingPlayerId, spell, flashbackCost, 0, false, null, false, false, false);
    }

    public CostModificationContext(GameData gameData, UUID castingPlayerId, Card spell,
                                   boolean flashbackCost, int xValue) {
        this(gameData, castingPlayerId, spell, flashbackCost, xValue, false, null, false, false, false);
    }

    public boolean fromGraveyard() {
        return sourceZone == Zone.GRAVEYARD;
    }
}
