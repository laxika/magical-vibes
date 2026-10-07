package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCantSacrificeTargetThisTurnEffect;
import org.springframework.stereotype.Component;

/** Records that the effect's controller can't sacrifice the target permanent this turn. */
@Component
public class ControllerCantSacrificeTargetThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControllerCantSacrificeTargetThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() != null) {
            gameData.sacrificeForbiddenThisTurn.put(entry.getTargetId(), entry.getControllerId());
        }
    }
}
