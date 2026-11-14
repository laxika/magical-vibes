package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.rules.CounterChangeEvent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.ReadAheadEffect;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.model.trigger.ChapterTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

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

    @Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;

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
        int maximumChapter = card.getSagaFinalChapter();
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
        for (int i = 0; i < loreCounters; i++) {
            gameData.rulesEvents.occur(CounterChangeEvent.of(CounterChangeEvent.Kind.YOU_PUT_LORE_COUNTER,
                    sagaPermanent, controllerId, CounterType.LORE, 1));
        }
        if (chapter == 1) {
            // A Saga entering normally has each lore counter placed in turn, so every chapter reached
            // by those counters triggers (Vorinclex doubling the entry counter reaches chapters I and II).
            for (int loreCount = 1; loreCount <= loreCounters; loreCount++) {
                triggerSagaChapter(gameData, sagaPermanent, card, controllerId, loreCount);
            }
            return;
        }
        triggerSagaChapter(gameData, sagaPermanent, card, controllerId, loreCounters);
    }

    private boolean hasReadAhead(GameData gameData, UUID controllerId, Permanent sagaPermanent) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .anyMatch(source -> gameQueryService.getActiveStaticEffects(gameData, source).stream()
                        .anyMatch(effect -> effect instanceof ReadAheadEffect readAhead
                                && (readAhead.appliesToAllControlledSagas()
                                || sagaPermanent != null && source.getId().equals(sagaPermanent.getId()))));
    }

    public void addLoreCounterAndTriggerChapter(GameData gameData, Permanent sagaPermanent,
                                                  Card card, UUID controllerId) {
        int loreCounters = sagaPermanent.getCounterCount(CounterType.LORE)
                + gameQueryService.replaceCounters(
                gameData, sagaPermanent, CounterType.LORE, 1, controllerId);
        sagaPermanent.setCounterCount(CounterType.LORE, loreCounters);
        renewLoreCounterPlayPermissions(gameData, sagaPermanent, controllerId);
        gameLogService.append(gameData,
                GameLog.cardThen(card, " gets a lore counter (" + loreCounters + ")."));
        log.info("Game {} - {} gets lore counter {}", gameData.id, card.getName(), loreCounters);
        gameData.rulesEvents.occur(CounterChangeEvent.of(CounterChangeEvent.Kind.YOU_PUT_LORE_COUNTER, sagaPermanent,
                controllerId, CounterType.LORE, 1));
        triggerSagaChapter(gameData, sagaPermanent, card, controllerId, loreCounters);
    }

    /**
     * Renews the play permission of cards this Saga exiled with "during any turn you put a lore counter
     * on this Saga, you may play that card" (Summon: Brynhildr); the permission lasts for this turn.
     */
    public void renewLoreCounterPlayPermissions(GameData gameData, Permanent sagaPermanent,
                                                  UUID controllerId) {
        gameData.exilePlayPermissionLoreCounterSagas.forEach((cardId, sagaId) -> {
            if (sagaId.equals(sagaPermanent.getId()) && gameData.findExiledCard(cardId) != null) {
                gameData.exilePlayPermissions.put(cardId, controllerId);
                gameData.exilePlayPermissionsExpireEndOfTurn.add(cardId);
            }
        });
    }

    /**
     * Triggers {@code card}'s chapter ability {@code loreCount} as lore counters reach it (CR 715.2b), and begins the
     * choices it goes on the stack with.
     */
    public void triggerSagaChapter(GameData gameData, Permanent sagaPermanent, Card card,
                                   UUID controllerId, int loreCount) {
        if (sagaPermanent != null && loreCount != sagaPermanent.getCounterCount(CounterType.LORE)
                && gameData.permanentsEnteredBattlefieldThisTurn.values().stream()
                .flatMap(List::stream).anyMatch(entered -> entered.getId().equals(card.getId()))
                && hasReadAhead(gameData, controllerId, sagaPermanent)) {
            return;
        }
        putChapterAbility(gameData, sagaPermanent, card, controllerId, loreCount, false);
        beginChapterChoices(gameData);
    }

    /** Triggers a chapter ability copied from a Saga card that is no longer on the battlefield. */
    public void triggerCopiedSagaChapter(GameData gameData, Card card, UUID controllerId,
                                         int chapterNumber) {
        putChapterAbility(gameData, null, card, controllerId, chapterNumber, true);
        beginChapterChoices(gameData);
    }

    /**
     * Puts {@code card}'s chapter ability {@code loreCount} on the stack through the stack door, whole (CR 603.3), with
     * the Saga as it was when it triggered and whether the chapter was copied ({@code null} Saga for one copied from a
     * Saga card that isn't on the battlefield): the door chooses its mode, its random mode and the targets its chapter
     * declares as it goes there (CR 603.3c, 603.3d). Reaching the final chapter triggers the "whenever the final chapter
     * ability of a Saga you control triggers" abilities. The choices it queued are begun by the caller.
     */
    public void putChapterAbility(GameData gameData, Permanent sagaPermanent, Card card, UUID controllerId,
                                  int loreCount, boolean copied) {
        if (loreCount < 1) {
            return;
        }
        ChapterTrigger chapter = Triggers.chapter(loreCount);
        List<TriggeredAbility> chapterAbilities = card.getTriggeredAbilities().stream()
                .filter(ability -> ability.hasTrigger(chapter)).toList();
        if (chapterAbilities.isEmpty()) {
            return;
        }
        Permanent snapshot = sagaPermanent == null ? null : new Permanent(sagaPermanent);
        String description = card.getName() + "'s chapter " + chapter.numeral() + " ability";
        for (TriggeredAbility ability : chapterAbilities) {
            boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                    ability, card, sagaPermanent, controllerId, description,
                    sagaPermanent == null ? ConditionContext.forCard(card, controllerId)
                            : ConditionContext.forPermanent(sagaPermanent, controllerId), entry -> {
                        entry.setNonTargeting(true);
                        if (snapshot != null) {
                            entry.setSourcePermanentSnapshot(snapshot);
                        }
                        entry.setCopy(copied);
                    }));
            if (triggered) {
                gameLogService.append(gameData, GameLog.cardThen(card,
                        "'s chapter " + chapter.numeral() + " ability triggers."));
                log.info("Game {} - {} chapter {} triggers", gameData.id, card.getName(), chapter.numeral());
            }
        }
        if (!copied && loreCount == card.getSagaFinalChapter()) {
            triggerCollectionService.checkSagaFinalChapterAbilityTriggers(gameData, controllerId,
                    card.getManaValue());
        }
    }

    /** Begins the target choices chapter abilities went to the stack door with, the chapters' own first. */
    public void beginChapterChoices(GameData gameData) {
        if (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        }
        if (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterPlayerTarget.class)) {
            triggerCollectionService.processNextSagaChapterPlayerTarget(gameData);
        }
        if (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterGraveyardTarget.class)) {
            triggerCollectionService.processNextSagaChapterGraveyardTarget(gameData);
        }
        triggerCollectionService.beginStackDoorChoices(gameData);
    }
}
