package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import com.github.laxika.magicalvibes.networking.model.StackEntryView;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GameStateMessage(
        MessageType type,
        GameStatus status,
        UUID activePlayerId,
        int turnNumber,
        TurnStep currentStep,
        UUID priorityPlayerId,
        List<List<PermanentView>> battlefields,
        List<StackEntryView> stack,
        List<List<CardView>> graveyards,
        List<Integer> deckSizes,
        List<Integer> handSizes,
        List<Integer> lifeTotals,
        List<CardView> hand,
        List<CardView> opponentHand,
        int mulliganCount,
        Map<String, Integer> manaPool,
        List<TurnStep> autoStopSteps,
        List<Integer> playableCardIndices,
        List<Integer> playableGraveyardLandIndices,
        List<String> newLogEntries
) {
    public GameStateMessage(
            GameStatus status,
            UUID activePlayerId,
            int turnNumber,
            TurnStep currentStep,
            UUID priorityPlayerId,
            List<List<PermanentView>> battlefields,
            List<StackEntryView> stack,
            List<List<CardView>> graveyards,
            List<Integer> deckSizes,
            List<Integer> handSizes,
            List<Integer> lifeTotals,
            List<CardView> hand,
            List<CardView> opponentHand,
            int mulliganCount,
            Map<String, Integer> manaPool,
            List<TurnStep> autoStopSteps,
            List<Integer> playableCardIndices,
            List<Integer> playableGraveyardLandIndices,
            List<String> newLogEntries
    ) {
        this(MessageType.GAME_STATE, status, activePlayerId, turnNumber, currentStep, priorityPlayerId,
                battlefields, stack, graveyards, deckSizes, handSizes, lifeTotals,
                hand, opponentHand, mulliganCount, manaPool, autoStopSteps, playableCardIndices,
                playableGraveyardLandIndices, newLogEntries);
    }
}
