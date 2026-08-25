package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EachPlayerMayExileGraveyardCardsState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayExileGraveyardCardsThenLoseLifeEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the APNAP graveyard choice and remaining-graveyard life loss. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayExileGraveyardCardsThenLoseLifeEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayExileGraveyardCardsThenLoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMayExileGraveyardCardsState state = gameData.eachPlayerMayExileGraveyardCards;
        if (!state.active) {
            state.active = true;
            state.currentPlayerId = null;
            state.remaining.clear();
            addInApnapOrder(gameData, state.remaining);
        } else {
            state.currentPlayerId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
        }

        processNextPlayer(gameData, entry, state);
    }

    private void processNextPlayer(GameData gameData, StackEntry entry,
                                   EachPlayerMayExileGraveyardCardsState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            state.currentPlayerId = playerId;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) {
                continue;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                    playerId,
                    List.copyOf(graveyard),
                    graveyard.size(),
                    "You may exile any number of cards from your graveyard for "
                            + entry.getCard().getName() + "."));
            return;
        }

        applyLifeLossForRemainingCards(gameData, entry);
        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private void applyLifeLossForRemainingCards(GameData gameData, StackEntry entry) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            int remainingCards = graveyard == null ? 0 : graveyard.size();
            if (remainingCards > 0) {
                lifeSupport.applyLifeLoss(gameData, playerId, remainingCards, entry.getCard().getName());
            }
        }
    }

    private static void addInApnapOrder(GameData gameData, java.util.Deque<UUID> order) {
        if (gameData.orderedPlayerIds.contains(gameData.activePlayerId)) {
            order.addLast(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                order.addLast(playerId);
            }
        }
    }
}
