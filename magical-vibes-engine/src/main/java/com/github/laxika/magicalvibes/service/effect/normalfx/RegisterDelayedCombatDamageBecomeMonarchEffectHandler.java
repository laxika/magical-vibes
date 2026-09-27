package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageBecomeMonarch;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageBecomeMonarchEffect;
import org.springframework.stereotype.Component;

@Component
public class RegisterDelayedCombatDamageBecomeMonarchEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCombatDamageBecomeMonarchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.queueDelayedAction(new DelayedCombatDamageBecomeMonarch(
                entry.getControllerId(), entry.getCard()));
    }
}
