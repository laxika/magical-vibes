package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReturnEnchantedCreatureAndSourceTransformedOnDeathEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var combined = (ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect) effect;
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            return;
        }

        entry.insertEffectsToResolve(effectIndex + 1, List.of(
                new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(combined.dyingCreatureCardId(), true),
                new ReturnSourceTransformedFromGraveyardEffect()
        ));
    }
}
