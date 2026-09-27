package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EachOpponentDrawsThenControllerDrawsState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDrawsThenControllerDrawsEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the draw count rider on Cut a Deal. */
@Component
public class EachOpponentDrawsThenControllerDrawsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    public EachOpponentDrawsThenControllerDrawsEffectHandler(PlayerInteractionSupport playerInteractionSupport) {
        this.playerInteractionSupport = playerInteractionSupport;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentDrawsThenControllerDrawsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentDrawsThenControllerDrawsState state = gameData.eachOpponentDrawsThenControllerDraws;
        if (!state.active) {
            state.active = true;
            state.controllerId = entry.getControllerId();
            state.remainingOpponentIds.addAll(orderedOpponents(gameData, entry.getControllerId()));
        }

        if (state.pendingOpponentId != null) {
            int drawn = drawnCardCount(gameData, state.pendingOpponentId) - state.pendingDrawCount;
            if (drawn == 0 && drawInstructionPaused(gameData)) {
                return;
            }
            if (drawn > 0) {
                state.successfulDrawCount++;
            }
            state.pendingOpponentId = null;
            state.pendingDrawCount = 0;
            if (drawInstructionPaused(gameData)) {
                return;
            }
        }

        while (!state.remainingOpponentIds.isEmpty()) {
            UUID opponentId = state.remainingOpponentIds.removeFirst();
            state.pendingOpponentId = opponentId;
            state.pendingDrawCount = drawnCardCount(gameData, opponentId);
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.applyDrawCards(gameData, opponentId, 1);
            if (drawInstructionPaused(gameData)) {
                return;
            }
            if (drawnCardCount(gameData, opponentId) > state.pendingDrawCount) {
                state.successfulDrawCount++;
            }
            state.pendingOpponentId = null;
            state.pendingDrawCount = 0;
        }

        int cardsToDraw = state.successfulDrawCount;
        UUID controllerId = state.controllerId;
        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
        playerInteractionSupport.applyDrawCards(gameData, controllerId, cardsToDraw);
    }

    private int drawnCardCount(GameData gameData, UUID playerId) {
        return gameData.cardsDrawnThisTurnIds.getOrDefault(playerId, List.of()).size();
    }

    private boolean drawInstructionPaused(GameData gameData) {
        return gameData.interaction.isAwaitingInput()
                || !gameData.pendingMayAbilities.isEmpty()
                || !gameData.pendingCardDraws.isEmpty();
    }

    private List<UUID> orderedOpponents(GameData gameData, UUID controllerId) {
        List<UUID> opponents = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && !activePlayerId.equals(controllerId)
                && gameData.playerIds.contains(activePlayerId)) {
            opponents.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)
                    && gameData.playerIds.contains(playerId)) {
                opponents.add(playerId);
            }
        }
        return opponents;
    }
}
