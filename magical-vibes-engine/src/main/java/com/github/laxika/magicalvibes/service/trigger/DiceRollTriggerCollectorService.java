package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiceRollTriggerCollectorService {

    private final GameLogService gameLogService;
    private final ConditionEvaluationService conditionEvaluationService;

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_ROLLS_HIGHEST_NATURAL_RESULT)
    private boolean handleControllerRollsHighestNaturalResult(TriggerMatchContext match, TriggerContext context) {
        return handleControllerRollsDice(match, context);
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE)
    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_ROLLS_DIE)
    private boolean handleControllerRollsDice(TriggerMatchContext match, TriggerContext context) {
        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : match.permanent().getCard();
        UUID sourcePermanentId = match.permanent() == null ? null : match.permanent().getId();
        if (match.admits(TargetPredicate.Kind.PERMANENT) || match.admits(TargetPredicate.Kind.PLAYER)) {
            CardEffect targeting = match.targetingEffect();
            int targetGroupIndex = sourceCard.getEffectTargetIndex(targeting);
            TargetFilter targetFilter = targetGroupIndex >= 0
                    ? sourceCard.getTargetGroups().get(targetGroupIndex).getFilter()
                    : sourceCard.getTargetFilter();
            boolean optionalTarget = match.targetIsOptional()
                    || isOptionalSingleTarget(sourceCard, targetGroupIndex);
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard,
                    match.controllerId(),
                    match.stackEffects(),
                    !match.admits(TargetPredicate.Kind.PERMANENT),
                    targetFilter,
                    0,
                    sourcePermanentId,
                    null,
                    optionalTarget,
                    null,
                    null,
                    match.controllerId(),
                    null,
                    match.sourcePlanarObject()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers on controller rolling dice and awaits a target",
                    match.gameData().id, sourceCard.getName());
            return true;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                sourcePermanentId);
        if (context instanceof TriggerContext.DiceRoll diceRoll) {
            entry.setEventValue(diceRoll.result());
        }
        if (match.sourcePlanarObject() != null) {
            entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
        }
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on controller rolling dice",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    private boolean isOptionalSingleTarget(Card sourceCard, int targetGroupIndex) {
        if (targetGroupIndex < 0 || targetGroupIndex >= sourceCard.getTargetGroups().size()) {
            return false;
        }
        var target = sourceCard.getTargetGroups().get(targetGroupIndex);
        return target.getMinTargets() == 0 && target.getMaxTargets() == 1;
    }
}
