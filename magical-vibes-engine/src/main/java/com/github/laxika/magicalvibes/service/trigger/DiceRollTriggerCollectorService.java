package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
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

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE)
    private boolean handleControllerRollsDice(TriggerMatchContext match, CardEffect effect,
                                               TriggerContext context) {
        if (effect instanceof ConditionalEffect conditional && conditional.interveningIf()
                && !conditionEvaluationService.isMet(match.gameData(), conditional.condition(),
                match.permanent() == null
                        ? ConditionContext.forCard(match.sourceCard(), match.controllerId())
                        : ConditionContext.forPermanent(match.permanent(), match.controllerId()))) {
            return false;
        }

        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : match.permanent().getCard();
        UUID sourcePermanentId = match.permanent() == null ? null : match.permanent().getId();
        TargetSpec targetSpec = effect.targetSpec();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                || targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            int targetGroupIndex = sourceCard.getEffectTargetIndex(effect);
            TargetFilter targetFilter = targetGroupIndex >= 0
                    ? sourceCard.getSpellTargets().get(targetGroupIndex).getFilter()
                    : sourceCard.getTargetFilter();
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard,
                    match.controllerId(),
                    new ArrayList<>(List.of(effect)),
                    !targetSpec.admits(TargetPredicate.Kind.PERMANENT),
                    targetFilter,
                    0,
                    sourcePermanentId));
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
                new ArrayList<>(List.of(effect)),
                null,
                sourcePermanentId);
        if (context instanceof TriggerContext.DiceRoll diceRoll && !diceRoll.planar()) {
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
}
