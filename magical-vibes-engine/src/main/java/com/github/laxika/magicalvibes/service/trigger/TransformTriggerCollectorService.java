package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects non-targeting triggers that watch an ally permanent transform. */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransformTriggerCollectorService {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_PERMANENT_TRANSFORMS)
    private boolean handleAllyPermanentTransform(TriggerMatchContext match, CardEffect effect,
                                                  TriggerContext context) {
        TriggerContext.PermanentTransforms transforms = (TriggerContext.PermanentTransforms) context;
        CardEffect resolved = effect;
        if (effect instanceof TriggeringCardConditionalEffect conditional
                && !predicateEvaluationService.matchesCardPredicate(
                        transforms.transformedCard(), conditional.predicate(), null,
                        match.gameData(), match.controllerId())) {
            return false;
        } else if (effect instanceof TriggeringCardConditionalEffect conditional) {
            resolved = conditional.wrapped();
        }

        Card sourceCard = match.permanent().getCard();
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(resolved)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when an ally permanent transforms",
                match.gameData().id, sourceCard.getName());
        return true;
    }
}
