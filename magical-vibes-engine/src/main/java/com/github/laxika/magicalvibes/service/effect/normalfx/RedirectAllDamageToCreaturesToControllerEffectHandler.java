package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageToCreaturesToControllerEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RedirectAllDamageToCreaturesToControllerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectAllDamageToCreaturesToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return;

        gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                null, null, CreatureDamageRedirectShield.UNLIMITED, controllerId));
    }
}
