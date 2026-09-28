package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtRandomEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random modal when it is used as an ordinary stack effect. */
@Component
@RequiredArgsConstructor
public class ChooseOneAtRandomEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOneAtRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ChooseOneAtRandomEffect randomEffect = (ChooseOneAtRandomEffect) effect;
        var option = randomEffect.options().get(
                ThreadLocalRandom.current().nextInt(randomEffect.options().size()));
        for (CardEffect optionEffect : option.effectsForSelection()) {
            EffectHandler handler = effectHandlerRegistry.getHandler(optionEffect);
            if (handler == null) {
                throw new IllegalStateException("No effect handler registered for "
                        + optionEffect.getClass().getSimpleName());
            }
            handler.resolve(gameData, entry, optionEffect);
        }
    }
}
