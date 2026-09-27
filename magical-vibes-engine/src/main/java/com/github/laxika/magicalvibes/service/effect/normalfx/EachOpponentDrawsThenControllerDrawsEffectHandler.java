package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EachOpponentDrawsThenControllerDrawsState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDrawsThenControllerDrawsEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves an opponent draw sequence followed by matching controller draws. */
@Component
public class EachOpponentDrawsThenControllerDrawsEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;

    public EachOpponentDrawsThenControllerDrawsEffectHandler(DrawService drawService) {
        this.drawService = drawService;
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
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(state.controllerId)) {
                    state.remainingOpponentIds.add(playerId);
                }
            }
        }

        if (state.controllerDrawPending) {
            if (drawInstructionPending(gameData)) {
                return;
            }
            finish(gameData, state);
            return;
        }

        if (state.currentOpponentId != null) {
            if (drawInstructionPending(gameData)) {
                return;
            }
            if (gameData.cardsDrawnThisTurn.getOrDefault(state.currentOpponentId, 0)
                    > state.cardsDrawnBeforeCurrentOpponent) {
                state.opponentsWhoDrew.add(state.currentOpponentId);
            }
            state.currentOpponentId = null;
        }

        while (!state.remainingOpponentIds.isEmpty()) {
            UUID opponentId = state.remainingOpponentIds.removeFirst();
            state.currentOpponentId = opponentId;
            state.cardsDrawnBeforeCurrentOpponent =
                    gameData.cardsDrawnThisTurn.getOrDefault(opponentId, 0);
            gameData.rerunCurrentEffectAfterInteraction = true;
            drawService.resolveDrawCard(gameData, opponentId);
            if (drawInstructionPending(gameData)) {
                return;
            }

            if (gameData.cardsDrawnThisTurn.getOrDefault(opponentId, 0)
                    > state.cardsDrawnBeforeCurrentOpponent) {
                state.opponentsWhoDrew.add(opponentId);
            }
            state.currentOpponentId = null;
            if (gameData.status == GameStatus.FINISHED) {
                return;
            }
        }

        state.controllerDrawPending = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        drawService.resolveDrawCards(gameData, state.controllerId, state.opponentsWhoDrew.size());
        if (drawInstructionPending(gameData) || gameData.status == GameStatus.FINISHED) {
            return;
        }
        finish(gameData, state);
    }

    private boolean drawInstructionPending(GameData gameData) {
        return gameData.interaction.isAwaitingInput()
                || !gameData.pendingMayAbilities.isEmpty()
                || !gameData.pendingCardDraws.isEmpty()
                || !gameData.pendingCommanderZoneMoves.isEmpty();
    }

    private void finish(GameData gameData, EachOpponentDrawsThenControllerDrawsState state) {
        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }
}
