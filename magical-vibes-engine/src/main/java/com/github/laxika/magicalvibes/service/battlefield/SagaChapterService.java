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
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReadAheadEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    @Lazy
    private PlayerInputService playerInputService;

    public SagaChapterService(GameQueryService gameQueryService,
                              GameLogService gameLogService,
                              @Lazy TriggerCollectionService triggerCollectionService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
    }

    public void initializeSaga(GameData gameData, Permanent sagaPermanent, Card card, UUID controllerId) {
        if (card.isBedtimeStory()) {
            return;
        }
        int maximumChapter = maximumChapter(card);
        if (maximumChapter > 0 && hasReadAhead(gameData, controllerId, sagaPermanent)) {
            playerInputService.beginNumberChoice(gameData, controllerId, sagaPermanent.getId(), 1, maximumChapter);
            return;
        }

        initializeSagaAtChapter(gameData, sagaPermanent, card, controllerId, 1);
    }

    public void completeReadAheadIfPresent(GameData gameData, Permanent sagaPermanent) {
        if (sagaPermanent == null || !sagaPermanent.getCard().isSaga()
                || sagaPermanent.getChosenNumber() < 1) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, sagaPermanent.getId());
        if (controllerId == null || !hasReadAhead(gameData, controllerId, sagaPermanent)) {
            return;
        }

        initializeSagaAtChapter(gameData, sagaPermanent, sagaPermanent.getCard(), controllerId,
                sagaPermanent.getChosenNumber());
    }

    private void initializeSagaAtChapter(GameData gameData, Permanent sagaPermanent, Card card,
                                         UUID controllerId, int chapter) {
        int loreCounters = gameQueryService.replaceCounters(
                gameData, sagaPermanent, CounterType.LORE, chapter, controllerId);
        sagaPermanent.setCounterCount(CounterType.LORE, loreCounters);
        gameLogService.append(gameData, GameLog.cardThen(card, " enters with " + chapter + " lore counter(s)."));
        log.info("Game {} - {} enters with lore counter(s) {}", gameData.id, card.getName(), chapter);
        triggerCollectionService.checkYouPutLoreCounterOnSagaTriggers(gameData, sagaPermanent, controllerId);
        triggerSagaChapter(gameData, sagaPermanent, card, controllerId, loreCounters);
    }

    private boolean hasReadAhead(GameData gameData, UUID controllerId, Permanent sagaPermanent) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .anyMatch(source -> gameQueryService.getActiveStaticEffects(gameData, source).stream()
                        .anyMatch(effect -> effect instanceof ReadAheadEffect readAhead
                                && (readAhead.appliesToAllControlledSagas()
                                || sagaPermanent != null && source.getId().equals(sagaPermanent.getId()))));
    }

    private int maximumChapter(Card card) {
        if (!card.getEffects(EffectSlot.SAGA_CHAPTER_VI).isEmpty()) return 6;
        if (!card.getEffects(EffectSlot.SAGA_CHAPTER_V).isEmpty()) return 5;
        if (!card.getEffects(EffectSlot.SAGA_CHAPTER_IV).isEmpty()) return 4;
        if (!card.getEffects(EffectSlot.SAGA_CHAPTER_III).isEmpty()) return 3;
        if (!card.getEffects(EffectSlot.SAGA_CHAPTER_II).isEmpty()) return 2;
        return card.getEffects(EffectSlot.SAGA_CHAPTER_I).isEmpty() ? 0 : 1;
    }

    public void addLoreCounterAndTriggerChapter(GameData gameData, Permanent sagaPermanent,
                                                  Card card, UUID controllerId) {
        int loreCounters = sagaPermanent.getCounterCount(CounterType.LORE)
                + gameQueryService.replaceCounters(
                gameData, sagaPermanent, CounterType.LORE, 1, controllerId);
        sagaPermanent.setCounterCount(CounterType.LORE, loreCounters);
        gameLogService.append(gameData,
                GameLog.cardThen(card, " gets a lore counter (" + loreCounters + ")."));
        log.info("Game {} - {} gets lore counter {}", gameData.id, card.getName(), loreCounters);
        triggerCollectionService.checkYouPutLoreCounterOnSagaTriggers(gameData, sagaPermanent, controllerId);
        triggerSagaChapter(gameData, sagaPermanent, card, controllerId, loreCounters);
    }

    /**
     * Triggers a Saga chapter, including its resolution-time target selection when required.
     */
    public void triggerSagaChapter(GameData gameData, Permanent sagaPermanent, Card card,
                                   UUID controllerId, int loreCount) {
        triggerSagaChapter(gameData, sagaPermanent, card, controllerId, loreCount, false);
    }

    /** Triggers a chapter ability copied from a Saga card that is no longer on the battlefield. */
    public void triggerCopiedSagaChapter(GameData gameData, Card card, UUID controllerId,
                                         int chapterNumber) {
        triggerSagaChapter(gameData, null, card, controllerId, chapterNumber, true);
    }

    private void triggerSagaChapter(GameData gameData, Permanent sagaPermanent, Card card,
                                    UUID controllerId, int loreCount, boolean copied) {
        if (!copied && sagaPermanent != null && loreCount != sagaPermanent.getCounterCount(CounterType.LORE)
                && gameData.permanentsEnteredBattlefieldThisTurn.values().stream()
                .flatMap(List::stream).anyMatch(entered -> entered.getId().equals(card.getId()))
                && hasReadAhead(gameData, controllerId, sagaPermanent)) {
            return;
        }
        EffectSlot chapterSlot = switch (loreCount) {
            case 1 -> EffectSlot.SAGA_CHAPTER_I;
            case 2 -> EffectSlot.SAGA_CHAPTER_II;
            case 3 -> EffectSlot.SAGA_CHAPTER_III;
            case 4 -> EffectSlot.SAGA_CHAPTER_IV;
            case 5 -> EffectSlot.SAGA_CHAPTER_V;
            case 6 -> EffectSlot.SAGA_CHAPTER_VI;
            default -> null;
        };
        if (chapterSlot == null) return;

        UUID sourcePermanentId = sagaPermanent == null ? null : sagaPermanent.getId();

        List<CardEffect> chapterEffects = card.getEffects(chapterSlot);
        if (chapterEffects.isEmpty()) return;

        String chapterName = switch (loreCount) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            default -> String.valueOf(loreCount);
        };

        if (chapterEffects.size() == 1
                && chapterEffects.getFirst() instanceof ChooseOneAtTriggerTimeEffect modal) {
            gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    card, controllerId, modal.choice(), sourcePermanentId));
            appendChapterTrigger(gameData, card, chapterName, "mode selection");
            return;
        }

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
                            new ArrayList<>(chapterEffects), sourcePermanentId, chapterName,
                            card.getSagaChapterTargetFilters(chapterSlot),
                            card.getSagaChapterTargetGroups(chapterSlot), List.of(), 0));
            appendChapterTrigger(gameData, card, chapterName, "grouped target selection");
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        } else if (needsPlayerTarget && needsPermanentTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    card, controllerId, new ArrayList<>(chapterEffects), false,
                    sagaChapterAnyTargetFilter(chapterEffects), 0, sourcePermanentId));
            appendChapterTrigger(gameData, card, chapterName, "any target selection");
            triggerCollectionService.processNextSpellTargetTrigger(gameData);
        } else if (needsPlayerTarget) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.SagaChapterPlayerTarget(card, controllerId,
                            new ArrayList<>(chapterEffects), sourcePermanentId, chapterName,
                            card.getSagaChapterTargetFilters(chapterSlot)));
            appendChapterTrigger(gameData, card, chapterName, "player target selection");
            triggerCollectionService.processNextSagaChapterPlayerTarget(gameData);
        } else if (needsPermanentTarget) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.SagaChapterTarget(card, controllerId,
                            new ArrayList<>(chapterEffects), sourcePermanentId, chapterName,
                            card.getSagaChapterTargetFilters(chapterSlot),
                            card.getSagaChapterTargetGroups(chapterSlot), List.of(), 0));
            appendChapterTrigger(gameData, card, chapterName, "target selection");
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        } else if (needsGraveyardTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SagaChapterGraveyardTarget(
                    card, controllerId, new ArrayList<>(chapterEffects), sourcePermanentId, chapterName));
            appendChapterTrigger(gameData, card, chapterName, "graveyard target selection");
            triggerCollectionService.processNextSagaChapterGraveyardTarget(gameData);
        } else {
            StackEntry chapterEntry;
            if (sourcePermanentId == null) {
                chapterEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s chapter " + chapterName + " ability",
                        new ArrayList<>(chapterEffects),
                        0,
                        (UUID) null);
            } else {
                chapterEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s chapter " + chapterName + " ability",
                        new ArrayList<>(chapterEffects),
                        null,
                        sourcePermanentId);
                if (sagaPermanent != null) {
                    chapterEntry.setSourcePermanentSnapshot(new Permanent(sagaPermanent));
                }
            }
            chapterEntry.setCopy(copied);
            gameData.stack.add(chapterEntry);
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
