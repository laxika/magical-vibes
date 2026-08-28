package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.BattlefieldAndGraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IndependentlyTargetedGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Answers multi-card selections routed through the graveyard interaction flow. The AI takes legal
 * cards up to the maximum, including zone-specific caps for mixed battlefield/graveyard choices.
 * Choices with overlapping target filters use a maximum one-to-one card/filter assignment.
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

        log.info("AI: Choosing {} cards from graveyard choice in game {}", chosen.size(), ctx.gameId());
        ctx.gameActions().answerInteraction(new InteractionAnswer.CardsChosen(chosen));
    }

    private List<UUID> chooseCards(List<UUID> validIds, int maxCount, AiInteractionContext ctx) {
        if (maxCount <= 0) {
            return List.of();
        }
        BattlefieldAndGraveyardCardChoosingEffect mixedZoneTargets = mixedZoneTargets(ctx.gameData());
        if (mixedZoneTargets != null) {
            return chooseMixedZoneCards(validIds, maxCount, mixedZoneTargets, ctx);
        }
        IndependentlyTargetedGraveyardCardsEffect independentTargets = independentTargets(ctx.gameData());
        if (independentTargets != null && independentTargets.requiresDistinctTargets()) {
            return chooseCardsForDistinctFilters(validIds, maxCount, independentTargets, ctx);
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

    private List<UUID> chooseMixedZoneCards(
            List<UUID> validIds,
            int maxCount,
            BattlefieldAndGraveyardCardChoosingEffect effect,
            AiInteractionContext ctx) {
        int battlefieldTargets = 0;
        int graveyardTargets = 0;
        List<UUID> chosen = new ArrayList<>();
        for (UUID cardId : validIds) {
            boolean inGraveyard = ctx.gameQueryService()
                    .findCardInGraveyardById(ctx.gameData(), cardId) != null;
            if (inGraveyard) {
                if (graveyardTargets >= effect.mixedZoneMaxGraveyardTargets()) {
                    continue;
                }
                graveyardTargets++;
            } else {
                if (battlefieldTargets >= effect.mixedZoneMaxBattlefieldTargets()) {
                    continue;
                }
                battlefieldTargets++;
            }
            chosen.add(cardId);
            if (chosen.size() == maxCount) {
                break;
            }
        }
        return chosen;
    }

    private List<UUID> chooseCardsForDistinctFilters(
            List<UUID> validIds,
            int maxCount,
            IndependentlyTargetedGraveyardCardsEffect effect,
            AiInteractionContext ctx) {
        Map<UUID, Card> cardsById = new LinkedHashMap<>();
        for (UUID cardId : validIds) {
            Card card = ctx.gameQueryService().findCardInGraveyardById(ctx.gameData(), cardId);
            if (card != null) {
                cardsById.put(cardId, card);
            }
        }

        List<CardPredicate> filters = effect.targetFilters();
        UUID[] cardIdsByFilter = new UUID[filters.size()];
        UUID sourceCardId = ctx.gameData().graveyardTargetOperation.card == null
                ? null
                : ctx.gameData().graveyardTargetOperation.card.getId();
        for (UUID cardId : cardsById.keySet()) {
            assignCardToFilter(cardId, cardsById, filters, cardIdsByFilter,
                    new boolean[filters.size()], sourceCardId, ctx);
        }
        return Arrays.stream(cardIdsByFilter)
                .filter(java.util.Objects::nonNull)
                .limit(maxCount)
                .toList();
    }

    private boolean assignCardToFilter(
            UUID cardId,
            Map<UUID, Card> cardsById,
            List<CardPredicate> filters,
            UUID[] cardIdsByFilter,
            boolean[] visitedFilters,
            UUID sourceCardId,
            AiInteractionContext ctx) {
        Card card = cardsById.get(cardId);
        for (int filterIndex = 0; filterIndex < filters.size(); filterIndex++) {
            if (visitedFilters[filterIndex]
                    || !ctx.gameQueryService().matchesCardPredicate(
                    card, filters.get(filterIndex), sourceCardId)) {
                continue;
            }
            visitedFilters[filterIndex] = true;
            UUID assignedCardId = cardIdsByFilter[filterIndex];
            if (assignedCardId == null
                    || assignCardToFilter(assignedCardId, cardsById, filters, cardIdsByFilter,
                    visitedFilters, sourceCardId, ctx)) {
                cardIdsByFilter[filterIndex] = cardId;
                return true;
            }
        }
        return false;
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

    private IndependentlyTargetedGraveyardCardsEffect independentTargets(GameData gameData) {
        CardEffect activeEffect = gameData.graveyardTargetOperation.activeSpellGraveyardChoiceEffect;
        List<CardEffect> effects = activeEffect == null
                ? gameData.graveyardTargetOperation.effects
                : List.of(activeEffect);
        if (effects == null) {
            return null;
        }
        return effects.stream()
                .filter(IndependentlyTargetedGraveyardCardsEffect.class::isInstance)
                .map(IndependentlyTargetedGraveyardCardsEffect.class::cast)
                .findFirst()
                .orElse(null);
    }

    private BattlefieldAndGraveyardCardChoosingEffect mixedZoneTargets(GameData gameData) {
        if (gameData.graveyardTargetOperation.effects == null) {
            return null;
        }
        return gameData.graveyardTargetOperation.effects.stream()
                .filter(BattlefieldAndGraveyardCardChoosingEffect.class::isInstance)
                .map(BattlefieldAndGraveyardCardChoosingEffect.class::cast)
                .findFirst()
                .orElse(null);
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
