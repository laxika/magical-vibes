package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerRummageState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KroxaDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Resolves Kroxa's opponent discard and nonland comparison. Each opponent chooses a card in APNAP
 * order, then all chosen cards are discarded simultaneously (CR 608.2f); opponents who did not
 * discard a nonland card then lose 3 life. Choices are deferred through {@link EachPlayerRummageState}
 * and the handler re-runs after each completed selection via {@code rerunCurrentEffectAfterInteraction}.
 * The losses follow the discards (CR 608.2c) as one {@link PlayersLoseLifeEffect} step, so the Lich's Duel
 * Mastery sacrifices that replace them are asked about (CR 616.1) after the discards and before any life is
 * lost, without discarding again.
 */
@Component
@RequiredArgsConstructor
public class KroxaDiscardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final ObjectProvider<CardChoiceHandlerService> cardChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KroxaDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerRummageState state = gameData.eachPlayerRummage;
        UUID controllerId = entry.getControllerId();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.deferDiscards = true;
            List<UUID> ordered = List.copyOf(gameData.orderedPlayerIds);
            int activeIndex = Math.max(0, ordered.indexOf(gameData.activePlayerId));
            for (int offset = 0; offset < ordered.size(); offset++) {
                UUID playerId = ordered.get((activeIndex + offset) % ordered.size());
                if (!playerId.equals(controllerId)) {
                    state.remaining.addLast(playerId);
                }
            }
            state.chosenAmounts.clear();
            for (UUID playerId : state.remaining) {
                state.chosenAmounts.put(playerId, 0);
            }
        }

        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.pollFirst();
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
            if (hand.isEmpty() || gameQueryService.isDiscardPrevented(gameData, playerId)
                    || !gameQueryService.canEffectCauseDiscard(gameData, playerId, controllerId)) {
                continue;
            }

            state.currentPlayerId = playerId;
            gameData.discardCausedByOpponent = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            state.currentPlayerId = null;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        finish(gameData, entry, state);
    }

    private void finish(GameData gameData, StackEntry entry, EachPlayerRummageState state) {
        UUID controllerId = entry.getControllerId();
        List<UUID> opponents = new ArrayList<>(state.chosenAmounts.keySet());
        List<EachPlayerRummageState.SelectedDiscard> selected = List.copyOf(state.selectedDiscards);

        Set<UUID> discardedNonland = new HashSet<>();
        for (EachPlayerRummageState.SelectedDiscard selection : selected) {
            gameData.playerHands.getOrDefault(selection.playerId(), List.of()).stream()
                    .filter(card -> card.getId().equals(selection.cardId()))
                    .findFirst()
                    .filter(card -> !card.hasType(CardType.LAND))
                    .ifPresent(card -> discardedNonland.add(selection.playerId()));
        }

        var discardedCounts = cardChoiceHandlerServiceProvider.getObject().discardCollectedCards(
                gameData, selected, controllerId);
        state.reset();
        gameData.discardCausedByOpponent = false;

        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (UUID playerId : opponents) {
            if (discardedCounts.getOrDefault(playerId, 0) > 0 && discardedNonland.contains(playerId)) {
                continue;
            }
            losses.put(playerId, 3);
        }
        if (!losses.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(new PlayersLoseLifeEffect(losses)));
        }
    }
}
