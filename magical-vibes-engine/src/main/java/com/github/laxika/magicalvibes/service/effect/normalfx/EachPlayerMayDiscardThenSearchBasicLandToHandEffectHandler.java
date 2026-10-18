package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EachPlayerMayDiscardThenSearchBasicLandState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardThenSearchBasicLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves Borderland Explorer's discard choices and conditional basic-land searches. Players choose
 * in APNAP order and the chosen cards are discarded together once everyone has chosen (CR 101.4).
 */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDiscardThenSearchBasicLandToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;

    @Autowired
    @Lazy
    private CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayDiscardThenSearchBasicLandToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMayDiscardThenSearchBasicLandToHandEffect discardEffect =
                (EachPlayerMayDiscardThenSearchBasicLandToHandEffect) effect;
        EachPlayerMayDiscardThenSearchBasicLandState state =
                gameData.eachPlayerMayDiscardThenSearchBasicLand;

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
            List<LibrarySearchFollowUp.BasicLandsPick> picks = cardChoiceHandlerService
                    .discardDeferredSelections(gameData, state.controllerId).keySet().stream()
                    .map(playerId -> new LibrarySearchFollowUp.BasicLandsPick(playerId, 1, false))
                    .toList();
            state.reset();
            gameData.rerunCurrentEffectAfterInteraction = false;
            if (!picks.isEmpty()) {
                basicLandSearchQueueSupport.advance(
                        gameData, LibrarySearchFollowUp.basicLandSearchesToHand(picks));
            }
        }
    }

    private void addApnapPlayers(GameData gameData, EachPlayerMayDiscardThenSearchBasicLandState state) {
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
                                 EachPlayerMayDiscardThenSearchBasicLandToHandEffect effect,
                                 EachPlayerMayDiscardThenSearchBasicLandState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            if (gameData.playerHands.getOrDefault(playerId, List.of()).isEmpty()) {
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
            int effectIndex = currentEffectIndex(entry, effect);
            entry.insertEffectsToResolve(effectIndex + 1, List.of(mayDiscard, effect));
            return;
        }
        state.active = false;
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
