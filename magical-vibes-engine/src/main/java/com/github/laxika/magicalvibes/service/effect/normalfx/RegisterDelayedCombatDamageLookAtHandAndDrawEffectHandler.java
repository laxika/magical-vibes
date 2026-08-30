package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLookAtHandAndDraw;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageLookAtHandAndDrawEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterDelayedCombatDamageLookAtHandAndDrawEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCombatDamageLookAtHandAndDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var delayedEffect = (RegisterDelayedCombatDamageLookAtHandAndDrawEffect) effect;
        gameData.queueDelayedAction(new DelayedCombatDamageLookAtHandAndDraw(
                entry.getControllerId(), entry.getCard(), delayedEffect.sourcePredicate()));
    }
}
