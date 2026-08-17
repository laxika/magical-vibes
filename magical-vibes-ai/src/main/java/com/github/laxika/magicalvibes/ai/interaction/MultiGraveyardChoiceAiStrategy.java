package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Answers multi-graveyard card selections: the AI takes the first legal cards up to the maximum.
 * When the choice must come from a single graveyard ("... from a single graveyard", Scarab Feast)
 * the AI confines its picks to one graveyard — the one holding the most selectable cards.
 */
@Slf4j
class MultiGraveyardChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.MultiGraveyardChoice> {

    @Override
    public Class<PendingInteraction.MultiGraveyardChoice> handledType() {
        return PendingInteraction.MultiGraveyardChoice.class;
    }

    @Override
    public void answer(PendingInteraction.MultiGraveyardChoice interaction, AiInteractionContext ctx) throws Exception {
        if (!ctx.aiPlayerId().equals(interaction.playerId())) {
            return;
        }

        List<UUID> validIds = interaction.validCardIds();
        if (validIds.isEmpty()) {
            return;
        }

        if (ctx.gameData().graveyardTargetOperation.singleGraveyard) {
            validIds = confineToSingleGraveyard(validIds, ctx);
        }

        List<UUID> chosen = chooseCards(validIds, interaction.maxCount(), ctx);

        log.info("AI: Choosing {} graveyard cards in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }

    private List<UUID> chooseCards(List<UUID> validIds, int maxCount, AiInteractionContext ctx) {
        if (maxCount <= 0) {
            return List.of();
        }
        Set<CardType> maxOnePerCardType = maxOnePerCardType(ctx.gameData());
        if (maxOnePerCardType.isEmpty()) {
            return validIds.stream().limit(maxCount).toList();
        }

        Map<CardType, Integer> selectedCounts = new EnumMap<>(CardType.class);
        List<UUID> chosen = new ArrayList<>();
        for (UUID cardId : validIds) {
            Card card = ctx.gameQueryService().findCardInGraveyardById(ctx.gameData(), cardId);
            if (card == null || hasReachedTypeLimit(card, maxOnePerCardType, selectedCounts)) {
                continue;
            }
            chosen.add(cardId);
            for (CardType cardType : maxOnePerCardType) {
                if (card.hasType(cardType)) {
                    selectedCounts.merge(cardType, 1, Integer::sum);
                }
            }
            if (chosen.size() == maxCount) {
                break;
            }
        }
        return chosen;
    }

    private boolean hasReachedTypeLimit(Card card, Set<CardType> maxOnePerCardType,
                                        Map<CardType, Integer> selectedCounts) {
        return maxOnePerCardType.stream()
                .anyMatch(cardType -> card.hasType(cardType)
                        && selectedCounts.getOrDefault(cardType, 0) >= 1);
    }

    private Set<CardType> maxOnePerCardType(GameData gameData) {
        if (gameData.graveyardTargetOperation.effects == null) {
            return Set.of();
        }
        return gameData.graveyardTargetOperation.effects.stream()
                .filter(ReturnTargetCardsFromGraveyardToHandEffect.class::isInstance)
                .map(ReturnTargetCardsFromGraveyardToHandEffect.class::cast)
                .map(ReturnTargetCardsFromGraveyardToHandEffect::maxOnePerCardType)
                .filter(types -> !types.isEmpty())
                .findFirst()
                .map(types -> Set.copyOf(types))
                .orElseGet(Set::of);
    }

    /** Keep only the cards belonging to whichever graveyard holds the most selectable cards. */
    private List<UUID> confineToSingleGraveyard(List<UUID> validIds, AiInteractionContext ctx) {
        Map<UUID, List<UUID>> byOwner = new LinkedHashMap<>();
        for (UUID cardId : validIds) {
            UUID owner = ctx.gameQueryService().findGraveyardOwnerById(ctx.gameData(), cardId);
            byOwner.computeIfAbsent(owner, k -> new java.util.ArrayList<>()).add(cardId);
        }
        return byOwner.values().stream()
                .max(java.util.Comparator.comparingInt(List::size))
                .orElse(validIds);
    }
}
