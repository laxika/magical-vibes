package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ContinueAfterDepartureEffect;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Runs the rest of a departing effect once its departure is done (see {@link ContinuesAfterDeparture}). */
@Component
@RequiredArgsConstructor
public class ContinueAfterDepartureEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ContinueAfterDepartureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CardEffect departing = ((ContinueAfterDepartureEffect) effect).departingEffect();
        if (effectHandlerRegistry.getHandler(departing) instanceof ContinuesAfterDeparture continuation) {
            continuation.continueAfterDeparture(gameData, entry, departing);
        }
    }
}
