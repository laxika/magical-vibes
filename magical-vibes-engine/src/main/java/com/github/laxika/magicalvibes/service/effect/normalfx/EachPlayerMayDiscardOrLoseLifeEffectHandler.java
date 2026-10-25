package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EachPlayerMayDiscardOneThenApplyEffectsState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves an APNAP "discard a card or lose life" choice for every player. Players choose in APNAP
 * order, the chosen cards are discarded together, and life is lost by every player who did not
 * discard once all players have chosen, including players with an empty hand (CR 101.4). The losses
 * follow the discards (CR 608.2c) as one {@link PlayersLoseLifeEffect} step, so the Lich's Duel Mastery
 * sacrifices that replace them are asked about (CR 616.1) after the discards and before any life is lost,
 * without discarding again.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDiscardOrLoseLifeEffectHandler implements NormalEffectHandlerBean {

    @Autowired
    @Lazy
    private CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayDiscardOrLoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMayDiscardOrLoseLifeEffect lifeEffect = (EachPlayerMayDiscardOrLoseLifeEffect) effect;
        EachPlayerMayDiscardOneThenApplyEffectsState state = gameData.eachPlayerMayDiscardOneThenApplyEffects;

        if (!state.active) {
            state.active = true;
            state.controllerId = entry.getControllerId();
            addApnapPlayers(gameData, state);
        } else if (state.currentPlayerId != null) {
            state.currentPlayerId = null;
            gameData.deferredDiscardChooserId = null;
            entry.setTargetId(null);
        }

        beginNextPlayer(gameData, entry, lifeEffect, state);
        if (!state.active) {
            finish(gameData, entry, lifeEffect, state);
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
                                 EachPlayerMayDiscardOrLoseLifeEffect lifeEffect,
                                 EachPlayerMayDiscardOneThenApplyEffectsState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
            if (hand.isEmpty()) {
                continue;
            }

            state.currentPlayerId = playerId;
            gameData.deferredDiscardChooserId = playerId;
            gameData.discardCausedByOpponent = !playerId.equals(state.controllerId);
            entry.setTargetId(playerId);

            MayEffect mayDiscard = new MayEffect(
                    new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                    "Discard a card? If you don't, you lose " + lifeEffect.lifeLoss() + " life.",
                    null,
                    MayChoicePlayer.TARGET_PLAYER);
            int effectIndex = currentEffectIndex(entry, lifeEffect);
            entry.insertEffectsToResolve(effectIndex + 1, List.of(mayDiscard, lifeEffect));
            return;
        }
        state.active = false;
    }

    private void finish(GameData gameData, StackEntry entry,
                        EachPlayerMayDiscardOrLoseLifeEffect lifeEffect,
                        EachPlayerMayDiscardOneThenApplyEffectsState state) {
        UUID controllerId = state.controllerId;
        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
        Map<UUID, List<Card>> discarded = cardChoiceHandlerService.discardDeferredSelections(gameData, controllerId);

        List<UUID> lifeLossPlayers = new ArrayList<>();
        if (gameData.activePlayerId != null && gameData.orderedPlayerIds.contains(gameData.activePlayerId)) {
            lifeLossPlayers.add(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                lifeLossPlayers.add(playerId);
            }
        }
        lifeLossPlayers.removeIf(discarded::containsKey);
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (UUID playerId : lifeLossPlayers) {
            losses.put(playerId, lifeEffect.lifeLoss());
        }
        if (!losses.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(new PlayersLoseLifeEffect(losses)));
        }
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
