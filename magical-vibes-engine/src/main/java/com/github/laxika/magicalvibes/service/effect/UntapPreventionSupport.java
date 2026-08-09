package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves self-scoped untap locks, including locks gated by a static condition. */
@Component
@RequiredArgsConstructor
public class UntapPreventionSupport {

    private final ConditionEvaluationService conditionEvaluationService;

    public boolean hasActiveSelfDoesntUntap(GameData gameData, Permanent permanent) {
        UUID controllerId = gameData.findControllerOf(permanent);
        return permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(effect -> hasActiveSelfDoesntUntap(gameData, permanent, controllerId, effect));
    }

    private boolean hasActiveSelfDoesntUntap(GameData gameData, Permanent permanent, UUID controllerId,
                                              CardEffect effect) {
        if (effect instanceof DoesntUntapEffect doesNotUntap) {
            return doesNotUntap.scope() == TapUntapScope.SELF;
        }
        if (effect instanceof ConditionalEffect conditional) {
            return conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(permanent, controllerId))
                    && hasActiveSelfDoesntUntap(gameData, permanent, controllerId, conditional.wrapped());
        }
        return false;
    }
}
