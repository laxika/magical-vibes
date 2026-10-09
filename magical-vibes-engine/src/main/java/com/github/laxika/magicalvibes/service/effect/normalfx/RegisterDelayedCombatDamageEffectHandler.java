package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageEffect;
import org.springframework.stereotype.Component;

@Component
public class RegisterDelayedCombatDamageEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RegisterDelayedCombatDamageEffect delayed = (RegisterDelayedCombatDamageEffect) effect;
        gameData.queueDelayedAction(new DelayedCombatDamageEffect(
                entry.getControllerId(), entry.getCard(), delayed.triggerEffect(), entry.getSourcePermanentId()));
    }
}
