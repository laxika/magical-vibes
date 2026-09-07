package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SagaChapterService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    public SagaChapterService(GameQueryService gameQueryService,
                              GameLogService gameLogService,
                              @Lazy TriggerCollectionService triggerCollectionService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
    }

    public void initializeSaga(GameData gameData, Permanent sagaPermanent, Card card, UUID controllerId) {
        int loreCounters = gameQueryService.replaceCounters(
                gameData, sagaPermanent, CounterType.LORE, 1, controllerId);
        sagaPermanent.setCounterCount(CounterType.LORE, loreCounters);
        gameLogService.append(gameData, GameLog.cardThen(card, " gets a lore counter (1)."));
        log.info("Game {} - {} enters with lore counter 1", gameData.id, card.getName());
        triggerSagaChapter(gameData, sagaPermanent, card, controllerId, 1);
    }

    /**
     * Triggers a Saga chapter, including its resolution-time target selection when required.
     */
    public void triggerSagaChapter(GameData gameData, Permanent sagaPermanent, Card card,
                                   UUID controllerId, int loreCount) {
        EffectSlot chapterSlot = switch (loreCount) {
            case 1 -> EffectSlot.SAGA_CHAPTER_I;
            case 2 -> EffectSlot.SAGA_CHAPTER_II;
            case 3 -> EffectSlot.SAGA_CHAPTER_III;
            case 4 -> EffectSlot.SAGA_CHAPTER_IV;
            case 5 -> EffectSlot.SAGA_CHAPTER_V;
            default -> null;
        };
        if (chapterSlot == null) return;

        List<CardEffect> chapterEffects = card.getEffects(chapterSlot);
        if (chapterEffects.isEmpty()) return;

        String chapterName = switch (loreCount) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(loreCount);
        };

        boolean needsPlayerTarget = chapterEffects.stream()
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                || card.getSagaChapterTargetFilters(chapterSlot).stream()
                .anyMatch(PlayerPredicateTargetFilter.class::isInstance);
        boolean hasSagaTargetGroups = !card.getSagaChapterTargetGroups(chapterSlot).isEmpty();
        boolean needsPermanentTarget = chapterEffects.stream()
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                || hasSagaTargetGroups;
        boolean needsGraveyardTarget = chapterEffects.stream().anyMatch(e ->
                e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                        || e instanceof ReturnTargetCardsFromGraveyardToHandEffect);
        if (hasSagaTargetGroups) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.SagaChapterTarget(card, controllerId,
                            new ArrayList<>(chapterEffects), sagaPermanent.getId(), chapterName,
                            card.getSagaChapterTargetFilters(chapterSlot),
                            card.getSagaChapterTargetGroups(chapterSlot), List.of(), 0));
            appendChapterTrigger(gameData, card, chapterName, "grouped target selection");
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        } else if (needsPlayerTarget && needsPermanentTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    card, controllerId, new ArrayList<>(chapterEffects), false,
                    sagaChapterAnyTargetFilter(chapterEffects), 0, sagaPermanent.getId()));
            appendChapterTrigger(gameData, card, chapterName, "any target selection");
            triggerCollectionService.processNextSpellTargetTrigger(gameData);
        } else if (needsPlayerTarget) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.SagaChapterPlayerTarget(card, controllerId,
                            new ArrayList<>(chapterEffects), sagaPermanent.getId(), chapterName,
                            card.getSagaChapterTargetFilters(chapterSlot)));
            appendChapterTrigger(gameData, card, chapterName, "player target selection");
            triggerCollectionService.processNextSagaChapterPlayerTarget(gameData);
        } else if (needsPermanentTarget) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.SagaChapterTarget(card, controllerId,
                            new ArrayList<>(chapterEffects), sagaPermanent.getId(), chapterName,
                            card.getSagaChapterTargetFilters(chapterSlot),
                            card.getSagaChapterTargetGroups(chapterSlot), List.of(), 0));
            appendChapterTrigger(gameData, card, chapterName, "target selection");
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        } else if (needsGraveyardTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SagaChapterGraveyardTarget(
                    card, controllerId, new ArrayList<>(chapterEffects), sagaPermanent.getId(), chapterName));
            appendChapterTrigger(gameData, card, chapterName, "graveyard target selection");
            triggerCollectionService.processNextSagaChapterGraveyardTarget(gameData);
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s chapter " + chapterName + " ability",
                    new ArrayList<>(chapterEffects),
                    null,
                    sagaPermanent.getId()
            ));
            appendChapterTrigger(gameData, card, chapterName, null);
        }
    }

    private void appendChapterTrigger(GameData gameData, Card card, String chapterName, String detail) {
        gameLogService.append(gameData, GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
        if (detail == null) {
            log.info("Game {} - {} chapter {} triggers", gameData.id, card.getName(), chapterName);
        } else {
            log.info("Game {} - {} chapter {} triggers (awaiting {})",
                    gameData.id, card.getName(), chapterName, detail);
        }
    }

    private TargetFilter sagaChapterAnyTargetFilter(List<CardEffect> chapterEffects) {
        CardEffect permanentTargetEffect = chapterEffects.stream()
                .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                .findFirst()
                .orElseThrow();
        var permanentPredicate = permanentTargetEffect.targetSpec().targetPredicate()
                .permanentRestriction().orElse(new PermanentTruePredicate());
        PlayerRelation relation = chapterEffects.stream()
                .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                .map(CardEffect::targetPlayerRelation)
                .findFirst()
                .orElse(PlayerRelation.ANY);
        return new AnyTargetPredicateTargetFilter(permanentPredicate,
                new PlayerRelationPredicate(relation), "target opponent or planeswalker");
    }
}
