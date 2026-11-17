package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCantSacrificeTargetThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
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
            gameData.addFloatingEffect(FloatingContinuousEffect.onPermanent(entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), effect, entry.getTargetId(),
                    Duration.UNTIL_END_OF_TURN));
        }
    }
}
