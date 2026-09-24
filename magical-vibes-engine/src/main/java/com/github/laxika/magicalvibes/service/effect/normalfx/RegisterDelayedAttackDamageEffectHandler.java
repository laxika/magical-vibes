package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedAttackDamage;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedAttackDamageEffect;
import org.springframework.stereotype.Component;

@Component
public class RegisterDelayedAttackDamageEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedAttackDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() == null) {
            return;
        }
        gameData.queueDelayedAction(new DelayedAttackDamage(
                entry.getControllerId(), entry.getTargetId(), entry.getSourcePermanentId(), entry.getCard()));
    }
}
