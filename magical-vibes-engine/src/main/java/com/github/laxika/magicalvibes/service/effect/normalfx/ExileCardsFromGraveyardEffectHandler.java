package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LastKnownCardStats;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Exiles the cards an {@link ExileCardsFromGraveyardEffect} targeted that are still legal targets (in a graveyard of its
 * scope, matching its filter; CR 608.2b), then applies its life riders. Never prompts. Records the
 * {@link StepKind#EXILE} step: the cards exiled and the owners of the graveyards they left.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileCardsFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardsFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileCardsFromGraveyardEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(controllerId);

        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        // Exile targeted cards that are still in graveyards
        int exiledCount = 0;
        List<UUID> exiledOwnerIds = new ArrayList<>();
        List<Card> exiledCards = new ArrayList<>();
        if (targetCardIds != null && !targetCardIds.isEmpty()) {
            List<Card> stillTargeted = targetCardIds.stream()
                    .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                    .filter(Objects::nonNull)
                    .toList();
            List<LastKnownCardStats> known = gameQueryService.asInGraveyard(gameData, stillTargeted);
            List<String> exiledNames = new ArrayList<>();
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                // "Cards from the defending player's graveyard": a card that moved to another graveyard
                // since the trigger chose it is a new object (CR 400.7) and is no longer the target.
                if (card != null && entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())
                        && !entry.getTargetId().equals(gameQueryService.findGraveyardOwnerById(gameData, cardId))) {
                    card = null;
                }
                UUID ownerId = card == null ? null : gameQueryService.findGraveyardOwnerById(gameData, cardId);
                if (card != null && e.ownGraveyardOnly() && !controllerId.equals(ownerId)) {
                    card = null;
                }
                if (card != null && e.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                        card, e.filter(), entry.getCard().getId())) {
                    card = null;
                }
                if (card != null) {
                    exiledNames.add(card.getName());
                    exiledCards.add(card);
                    UUID sourcePermanentId = e.trackWithSource()
                            ? entry.getSourcePermanentId() : null;
                    if (graveyardReturnSupport.exileCardFromAnyGraveyard(
                            gameData, cardId, card, sourcePermanentId)
                            && e.putKickCountersOnExiledCards()) {
                        gameData.addExiledCardCounters(card.getId(), CounterType.KICK, 1);
                    }
                    if (ownerId != null && !exiledOwnerIds.contains(ownerId)) {
                        exiledOwnerIds.add(ownerId);
                    }
                }
            }
            exiledCount = exiledNames.size();
            entry.recordStep(StepKind.EXILE, StepOutcome.ofObjects(
                    exiledCards.stream().map(Card::getId).toList(), exiledOwnerIds)
                    .withLastKnownCards(known.stream()
                            .filter(card -> exiledCards.stream().anyMatch(c -> c.getId().equals(card.cardId())))
                            .toList()));
            if (!exiledNames.isEmpty()) {
                String logEntry = playerName + " exiles " + String.join(", ", exiledNames) + " from graveyard.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} exiled {} cards from graveyards", gameData.id, playerName, exiledNames.size());
            }
        }

        // Gain life after exile — flat, or once per card actually exiled this way (Rysorian Badger).
        int lifeGain = e.lifeGainPerExiledCard() ? e.lifeGain() * exiledCount : e.lifeGain();
        if (lifeGain > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, lifeGain);
        }

        int conditionalMatchCount = e.conditionalFilter() == null ? 0
                : (int) exiledCards.stream().filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, e.conditionalFilter(), entry.getCard().getId())).count();
        if (conditionalMatchCount > 0) {
            int riderMultiplier = e.conditionalLifePerMatchingCard() ? conditionalMatchCount : 1;
            if (e.conditionalLifeLossEachOpponent() > 0) {
                // CR 608.2c: "each opponent loses N life and you gain N life" follows the exile, the loss first.
                // As steps of their own, a Lich's Duel Mastery sacrifice replacing a loss asks its CR 616.1
                // questions without exiling again.
                List<CardEffect> steps = new ArrayList<>();
                steps.add(new LoseLifeEffect(e.conditionalLifeLossEachOpponent() * riderMultiplier,
                        Subjects.eachOpponent()));
                if (e.conditionalLifeGain() > 0) {
                    steps.add(new GainLifeEffect(e.conditionalLifeGain() * riderMultiplier));
                }
                entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, steps);
            } else if (e.conditionalLifeGain() > 0) {
                lifeSupport.applyGainLife(gameData, controllerId,
                        e.conditionalLifeGain() * riderMultiplier);
            }
        }

        // "If you do, this creature assigns no combat damage this turn" — only when something was
        // actually exiled, and only for the ability's source permanent (the unblocked attacker).
        if (e.assignNoCombatDamage() && exiledCount > 0 && entry.getSourcePermanentId() != null) {
            PreventionSupport.preventCombatDamageDealtBy(gameData, entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), entry.getSourcePermanentId(),
                    Duration.UNTIL_END_OF_TURN);
            PreventionSupport.assignNoCombatDamage(gameData, entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), entry.getSourcePermanentId(), Duration.UNTIL_END_OF_TURN);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " assigns no combat damage this turn."));
            log.info("Game {} - {} assigns no combat damage this turn", gameData.id, entry.getCard().getName());
        }
    }
}
