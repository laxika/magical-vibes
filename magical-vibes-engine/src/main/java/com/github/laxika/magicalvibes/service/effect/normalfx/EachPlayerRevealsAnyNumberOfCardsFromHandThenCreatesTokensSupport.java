package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentControlSupport permanentControlSupport;

    public boolean beginNextChoice(GameData gameData, StackEntry entry, List<UUID> remainingPlayerIds,
                                   List<UUID> playerOrder, Map<UUID, Integer> revealedCounts,
                                   EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect effect) {
        for (int i = 0; i < remainingPlayerIds.size(); i++) {
            UUID playerId = remainingPlayerIds.get(i);
            List<UUID> validCardIds = validCardIds(gameData, playerId, effect);
            if (validCardIds.isEmpty()) {
                continue;
            }

            List<UUID> remaining = new ArrayList<>(remainingPlayerIds.subList(i + 1, remainingPlayerIds.size()));
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.RevealAnyNumberOfCardsFromHandChoice(
                            playerId, validCardIds, entry.getCard().getName(), null, null,
                            new PendingInteraction.EachPlayerRevealContext(
                                    remaining, playerOrder, revealedCounts, effect.filter(), effect.token())));
            return true;
        }

        createTokens(gameData, entry, playerOrder, revealedCounts, effect.token());
        return false;
    }

    private void createTokens(GameData gameData, StackEntry entry, List<UUID> playerOrder,
                              Map<UUID, Integer> revealedCounts, CreateTokenEffect token) {
        for (UUID playerId : playerOrder) {
            int count = revealedCounts.getOrDefault(playerId, 0);
            if (count <= 0) {
                continue;
            }
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, playerId, token.withAmount(count), entry.getCard().getSetCode()));
        }
    }

    private List<UUID> validCardIds(GameData gameData, UUID playerId,
                                    EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect effect) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return List.of();
        }
        return hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), card.getId(), gameData, playerId))
                .map(Card::getId)
                .toList();
    }

    public static List<UUID> apnapOrder(GameData gameData) {
        List<UUID> orderedPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayerIds.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayerIds;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayerIds.subList(activeIndex, orderedPlayerIds.size()));
        rotated.addAll(orderedPlayerIds.subList(0, activeIndex));
        return rotated;
    }

    public boolean beginInitialChoice(GameData gameData, StackEntry entry,
                                      EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect effect) {
        List<UUID> order = apnapOrder(gameData);
        return beginNextChoice(gameData, entry, order, order, Map.of(), effect);
    }
}
