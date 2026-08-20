package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import java.util.List;
import org.springframework.stereotype.Component;

/** Resolves a controller mill followed by a reflexive triggered ability. */
@Component
public class MillControllerThenEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MillControllerThenEffect millThen = (MillControllerThenEffect) effect;
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("MillControllerThenEffect is not part of the resolving entry");
        }

        entry.insertEffectsToResolve(effectIndex + 1, List.of(
                new MillEffect(millThen.count(), MillRecipient.CONTROLLER),
                new QueueReflexiveAbilityEffect(millThen.thenEffect())));
    }
}
