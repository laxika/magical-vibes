package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilThenEffect;
import java.util.List;
import org.springframework.stereotype.Component;

/** Resolves surveil followed by a reflexive triggered ability. */
@Component
public class SurveilThenEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SurveilThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SurveilThenEffect surveilThen = (SurveilThenEffect) effect;
        if (surveilThen.count() <= 0) {
            return;
        }

        if (!surveilThen.queueReflexiveAbility()) {
            entry.setEventValue(0);
        }

        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("SurveilThenEffect is not part of the resolving entry");
        }

        CardEffect continuation = surveilThen.queueReflexiveAbility()
                ? new QueueReflexiveAbilityEffect(surveilThen.thenEffect())
                : surveilThen.thenEffect();
        entry.insertEffectsToResolve(effectIndex + 1, List.of(
                new SurveilEffect(surveilThen.count()),
                continuation));
    }
}
