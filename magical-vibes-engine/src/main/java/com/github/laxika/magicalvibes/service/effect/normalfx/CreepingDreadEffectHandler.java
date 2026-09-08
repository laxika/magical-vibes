package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreepingDreadState;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreepingDreadEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Creeping Dread's simultaneous-information discard comparison. */
@Component
@RequiredArgsConstructor
public class CreepingDreadEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreepingDreadEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreepingDreadState state = gameData.creepingDread;
        if (!state.active) {
            state.active = true;
            state.controllerId = entry.getControllerId();
            state.remaining.add(gameData.activePlayerId);
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    state.remaining.add(playerId);
                }
            }
            beginNextDiscard(gameData, entry, state);
            return;
        }

        if (state.currentPlayerId != null) {
            state.discardedCardTypes.put(state.currentPlayerId,
                    Set.copyOf(gameData.lastDiscardedCardTypes));
            state.currentPlayerId = null;
        }
        beginNextDiscard(gameData, entry, state);
    }

    private void beginNextDiscard(GameData gameData, StackEntry entry, CreepingDreadState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            state.currentPlayerId = playerId;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                state.currentPlayerId = null;
                continue;
            }

            gameData.discardCausedByOpponent = !playerId.equals(state.controllerId);
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            return;
        }

        finish(gameData, entry, state);
    }

    private void finish(GameData gameData, StackEntry entry, CreepingDreadState state) {
        UUID controllerId = state.controllerId;
        Set<CardType> controllerTypes = state.discardedCardTypes.get(controllerId);
        List<UUID> lifeLossPlayers = new ArrayList<>();
        if (controllerTypes != null && !controllerTypes.isEmpty()) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (playerId.equals(controllerId)) {
                    continue;
                }
                Set<CardType> opponentTypes = state.discardedCardTypes.get(playerId);
                if (opponentTypes != null && controllerTypes.stream().anyMatch(opponentTypes::contains)) {
                    lifeLossPlayers.add(playerId);
                }
            }
        }

        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
        for (UUID playerId : lifeLossPlayers) {
            lifeSupport.applyLifeLoss(gameData, playerId, 3, entry.getCard().getName());
        }
    }
}
