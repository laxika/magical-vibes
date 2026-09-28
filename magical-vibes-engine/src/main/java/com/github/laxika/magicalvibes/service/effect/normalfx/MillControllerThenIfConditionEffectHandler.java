package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfConditionEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a controller mill followed by a condition-gated reflexive triggered ability. */
@Component
@RequiredArgsConstructor
public class MillControllerThenIfConditionEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerThenIfConditionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MillControllerThenIfConditionEffect millThen = (MillControllerThenIfConditionEffect) effect;
        graveyardService.resolveMillPlayer(gameData, entry.getControllerId(), millThen.count());

        Condition condition = millThen.condition();
        if (!conditionEvaluationService.isMet(gameData, condition,
                ConditionContext.forStackEntry(entry), entry.getEventValue())) {
            return;
        }

        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("MillControllerThenIfConditionEffect is not part of the resolving entry");
        }

        entry.insertEffectsToResolve(effectIndex + 1, List.of(
                new QueueReflexiveAbilityEffect(new ConditionalEffect(condition, millThen.thenEffect()))));
    }
}
