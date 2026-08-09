package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllPermanentsAsEntersEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SacrificeAllPermanentsAsEntersEffectHandler {

    private final DestructionSupport destructionSupport;

    public void applyIfPresent(GameData gameData, UUID controllerId, Permanent enteringPermanent) {
        SacrificeAllPermanentsAsEntersEffect effect = enteringPermanent.getCard()
                .getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(SacrificeAllPermanentsAsEntersEffect.class::isInstance)
                .map(SacrificeAllPermanentsAsEntersEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return;
        }

        destructionSupport.sacrificePlayerMatchingPermanents(
                gameData, controllerId, Integer.MAX_VALUE, effect.filter());
    }
}
