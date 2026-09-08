package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalIfEventValueAtLeastEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetLifeTotalIfEventValueAtLeastEffectHandler implements NormalEffectHandlerBean {

    private final SetLifeTotalEffectHandler setLifeTotalEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetLifeTotalIfEventValueAtLeastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SetLifeTotalIfEventValueAtLeastEffect conditional = (SetLifeTotalIfEventValueAtLeastEffect) effect;
        if (entry.getEventValue() >= conditional.threshold()) {
            setLifeTotalEffectHandler.resolve(gameData, entry, new SetLifeTotalEffect(conditional.lifeTotal()));
        }
    }
}
