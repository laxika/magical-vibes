package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EachPlayerMayDiscardOneThenApplyEffectsState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardOneThenApplyEffectsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves each player's optional discard and the shared discard-type riders. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDiscardOneThenApplyEffectsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayDiscardOneThenApplyEffectsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMayDiscardOneThenApplyEffectsEffect discardEffect =
                (EachPlayerMayDiscardOneThenApplyEffectsEffect) effect;
        EachPlayerMayDiscardOneThenApplyEffectsState state =
                gameData.eachPlayerMayDiscardOneThenApplyEffects;

        if (!state.active) {
            state.active = true;
            state.controllerId = entry.getControllerId();
            addApnapPlayers(gameData, state);
        } else if (state.currentPlayerId != null) {
            recordDiscard(gameData, state);
            state.currentPlayerId = null;
            entry.setTargetId(null);
        }

        beginNextPlayer(gameData, entry, discardEffect, state);
        if (!state.active) {
            finish(gameData, entry, discardEffect, state);
        }
    }

    private void addApnapPlayers(GameData gameData, EachPlayerMayDiscardOneThenApplyEffectsState state) {
        if (gameData.activePlayerId != null && gameData.playerIds.contains(gameData.activePlayerId)) {
            state.remaining.add(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                state.remaining.add(playerId);
            }
        }
    }

    private void beginNextPlayer(GameData gameData, StackEntry entry,
                                 EachPlayerMayDiscardOneThenApplyEffectsEffect discardEffect,
                                 EachPlayerMayDiscardOneThenApplyEffectsState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
            if (hand.isEmpty()) {
                continue;
            }

            state.currentPlayerId = playerId;
            state.currentDiscardCountBefore = gameData.cardsDiscardedThisTurn.getOrDefault(playerId, 0);
            gameData.discardCausedByOpponent = !playerId.equals(state.controllerId);
            entry.setTargetId(playerId);

            MayEffect mayDiscard = new MayEffect(
                    new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                    "Discard a card?",
                    null,
                    MayChoicePlayer.TARGET_PLAYER);
            int effectIndex = currentEffectIndex(entry, discardEffect);
            entry.insertEffectsToResolve(effectIndex + 1, List.of(mayDiscard, discardEffect));
            return;
        }
        state.active = false;
    }

    private void recordDiscard(GameData gameData, EachPlayerMayDiscardOneThenApplyEffectsState state) {
        UUID playerId = state.currentPlayerId;
        int discardedAfter = gameData.cardsDiscardedThisTurn.getOrDefault(playerId, 0);
        if (discardedAfter <= state.currentDiscardCountBefore) {
            return;
        }

        state.playersWhoDiscarded.add(playerId);
        Set<CardType> discardedTypes = gameData.lastDiscardedCardTypes;
        state.creatureCardDiscarded |= discardedTypes.contains(CardType.CREATURE);
        state.nonCreatureCardDiscarded |= !discardedTypes.contains(CardType.CREATURE);
    }

    private void finish(GameData gameData, StackEntry entry,
                        EachPlayerMayDiscardOneThenApplyEffectsEffect discardEffect,
                        EachPlayerMayDiscardOneThenApplyEffectsState state) {
        List<CardEffect> followUps = new ArrayList<>();
        for (UUID playerId : state.playersWhoDiscarded) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, 1);
        }
        if (state.creatureCardDiscarded) {
            followUps.add(discardEffect.creatureDiscardEffect());
        }
        if (state.nonCreatureCardDiscarded) {
            followUps.add(discardEffect.nonCreatureDiscardEffect());
        }

        int effectIndex = currentEffectIndex(entry, discardEffect);
        if (!followUps.isEmpty()) {
            entry.insertEffectsToResolve(effectIndex + 1, followUps);
        }
        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private int currentEffectIndex(StackEntry entry, CardEffect effect) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = effects.size() - 1; i >= 0; i--) {
            if (effects.get(i) == effect) {
                return i;
            }
        }
        throw new IllegalStateException("Resolving effect is not present in its stack entry");
    }
}
