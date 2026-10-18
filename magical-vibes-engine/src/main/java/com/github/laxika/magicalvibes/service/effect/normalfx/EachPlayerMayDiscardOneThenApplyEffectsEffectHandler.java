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
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves each player's optional discard and the shared discard-type riders. Players choose in APNAP
 * order and the chosen cards are discarded together once everyone has chosen (CR 101.4).
 */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDiscardOneThenApplyEffectsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Autowired
    @Lazy
    private CardChoiceHandlerService cardChoiceHandlerService;

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
            state.currentPlayerId = null;
            gameData.deferredDiscardChooserId = null;
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
            gameData.deferredDiscardChooserId = playerId;
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

    private void finish(GameData gameData, StackEntry entry,
                        EachPlayerMayDiscardOneThenApplyEffectsEffect discardEffect,
                        EachPlayerMayDiscardOneThenApplyEffectsState state) {
        Map<UUID, List<Card>> discarded =
                cardChoiceHandlerService.discardDeferredSelections(gameData, state.controllerId);
        boolean creatureCardDiscarded = false;
        boolean nonCreatureCardDiscarded = false;
        for (List<Card> cards : discarded.values()) {
            for (Card card : cards) {
                creatureCardDiscarded |= card.hasType(CardType.CREATURE);
                nonCreatureCardDiscarded |= !card.hasType(CardType.CREATURE);
            }
        }
        for (UUID playerId : discarded.keySet()) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, 1);
        }
        List<CardEffect> followUps = new ArrayList<>();
        if (creatureCardDiscarded) {
            followUps.add(discardEffect.creatureDiscardEffect());
        }
        if (nonCreatureCardDiscarded) {
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
