package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Collects triggers caused by events involving cards suspended in exile. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExiledCardTriggerCollectorService {

    private final GameLogService gameLogService;
    private final ConditionEvaluationService conditionEvaluationService;

    @CollectsTrigger(value = ConditionalEffect.class, slot = EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE)
    private boolean handleConditionalTimeCounterRemoved(TriggerMatchContext match,
            ConditionalEffect conditional, TriggerContext ctx) {
        if (conditional.interveningIf()
                && !conditionEvaluationService.isMet(match.gameData(), conditional.condition(),
                        ConditionContext.forCard(match.sourceCard(), match.controllerId()))) {
            return false;
        }
        return handleTimeCounterRemoved(match, conditional, ctx);
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE)
    private boolean handleTimeCounterRemoved(TriggerMatchContext match, CardEffect effect,
            TriggerContext ctx) {
        TriggerContext.TimeCounterRemovedFromExile removed =
                (TriggerContext.TimeCounterRemovedFromExile) ctx;
        Card card = match.sourceCard();
        if (removed.remainingCounters() < 0 || card == null) {
            return false;
        }

        List<CardEffect> triggeredEffects = card.getEffects(EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE);
        if (triggeredEffects.isEmpty() || triggeredEffects.getFirst() != effect) {
            return true;
        }

        GameData gameData = match.gameData();
        if (triggeredEffects.stream().anyMatch(triggeredEffect ->
                triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    card, match.controllerId(), new ArrayList<>(triggeredEffects), "time-counter-removed"));
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            log.info("Game {} - {} triggers when a time counter is removed from exile and awaits a target",
                    gameData.id, card.getName());
            return true;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                match.controllerId(),
                card.getName() + "'s ability",
                new ArrayList<>(triggeredEffects));
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(card));
        log.info("Game {} - {} triggers when a time counter is removed from exile",
                gameData.id, card.getName());
        return true;
    }
}
