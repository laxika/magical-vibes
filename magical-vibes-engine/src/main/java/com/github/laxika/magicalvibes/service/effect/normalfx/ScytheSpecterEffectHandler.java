package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ScytheSpecterState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ScytheSpecterEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves Scythe Specter: each opponent chooses a card in APNAP order, the chosen cards are
 * discarded together once everyone has chosen, then the opponents who discarded a card with the
 * greatest mana value lose that much life. The losses follow the discards (CR 608.2c) as one
 * {@link PlayersLoseLifeEffect} step, so the Lich's Duel Mastery sacrifices that replace them are
 * asked about (CR 616.1) after the discards and before any life is lost, without discarding again.
 */
@Component
@RequiredArgsConstructor
public class ScytheSpecterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Autowired
    @Lazy
    private CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ScytheSpecterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ScytheSpecterState state = gameData.scytheSpecter;
        if (!state.active) {
            state.active = true;
            state.controllerId = entry.getControllerId();
            addOpponentsInApnapOrder(gameData, state);
        } else if (state.currentPlayerId != null) {
            state.currentPlayerId = null;
            gameData.deferredDiscardChooserId = null;
        }

        processNextOpponent(gameData, entry, state);
    }

    private void processNextOpponent(GameData gameData, StackEntry entry,
            ScytheSpecterState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            state.currentPlayerId = playerId;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()
                    || gameQueryService.isDiscardPrevented(gameData, playerId)) {
                state.currentPlayerId = null;
                continue;
            }

            gameData.discardCausedByOpponent = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.deferredDiscardChooserId = playerId;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            state.currentPlayerId = null;
            gameData.deferredDiscardChooserId = null;
        }

        finish(gameData, entry, state);
    }

    private void finish(GameData gameData, StackEntry entry, ScytheSpecterState state) {
        gameData.discardCausedByOpponent = true;
        Map<UUID, List<Card>> discarded =
                cardChoiceHandlerService.discardDeferredSelections(gameData, entry.getControllerId());
        Map<UUID, Integer> manaValues = new HashMap<>();
        discarded.forEach((playerId, cards) -> {
            List<Card> library = gameData.playerDecks.get(playerId);
            for (Card card : cards) {
                boolean putIntoHiddenLibrary = library != null && !library.isEmpty()
                        && library.getFirst() == card;
                if (!putIntoHiddenLibrary) {
                    manaValues.merge(playerId, card.getManaValue(), Math::max);
                }
            }
        });
        int greatestManaValue = manaValues.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        if (greatestManaValue > 0) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                Integer manaValue = manaValues.get(playerId);
                if (manaValue != null && manaValue == greatestManaValue) {
                    losses.put(playerId, greatestManaValue);
                }
            }
        }

        state.reset();
        gameData.discardCausedByOpponent = false;
        gameData.rerunCurrentEffectAfterInteraction = false;
        if (!losses.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(new PlayersLoseLifeEffect(losses)));
        }
    }

    private void addOpponentsInApnapOrder(GameData gameData, ScytheSpecterState state) {
        List<UUID> orderedPlayers = List.copyOf(gameData.orderedPlayerIds);
        int activePlayerIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activePlayerIndex < 0) {
            activePlayerIndex = 0;
        }
        for (int offset = 0; offset < orderedPlayers.size(); offset++) {
            UUID playerId = orderedPlayers.get((activePlayerIndex + offset) % orderedPlayers.size());
            if (!playerId.equals(state.controllerId)) {
                state.remaining.addLast(playerId);
            }
        }
    }
}
