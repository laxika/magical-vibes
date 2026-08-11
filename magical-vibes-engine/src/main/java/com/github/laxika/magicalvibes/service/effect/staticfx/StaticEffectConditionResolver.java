package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves conditional static effects for engine code that consumes effects directly. */
@Component
@RequiredArgsConstructor
public class StaticEffectConditionResolver {

    private final ConditionEvaluationService conditionEvaluationService;

    public CardEffect resolve(GameData gameData, Permanent source, UUID sourceControllerId, CardEffect effect) {
        CardEffect current = effect;
        while (current instanceof ConditionalEffect conditional) {
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(source, sourceControllerId))) {
                return null;
            }
            current = conditional.wrapped();
        }
        return current;
    }
}
