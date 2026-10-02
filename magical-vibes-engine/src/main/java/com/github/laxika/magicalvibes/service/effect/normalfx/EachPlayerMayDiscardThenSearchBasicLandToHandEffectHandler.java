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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Borderland Explorer's discard choices and conditional basic-land searches. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDiscardThenSearchBasicLandToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;

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
            int discardedAfter = gameData.cardsDiscardedThisTurn
                    .getOrDefault(state.currentPlayerId, 0);
            if (discardedAfter > state.currentDiscardCountBefore) {
                state.playersWhoDiscarded.add(state.currentPlayerId);
            }
            state.currentPlayerId = null;
            entry.setTargetId(null);
        }

        beginNextPlayer(gameData, entry, discardEffect, state);
        if (!state.active) {
            List<LibrarySearchFollowUp.BasicLandsPick> picks = state.playersWhoDiscarded.stream()
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
            state.currentDiscardCountBefore = gameData.cardsDiscardedThisTurn.getOrDefault(playerId, 0);
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
