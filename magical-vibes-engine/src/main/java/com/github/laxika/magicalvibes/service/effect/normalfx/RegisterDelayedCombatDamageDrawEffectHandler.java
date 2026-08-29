package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageDraw;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageDrawEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterDelayedCombatDamageDrawEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCombatDamageDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var drawEffect = (RegisterDelayedCombatDamageDrawEffect) effect;
        gameData.queueDelayedAction(new DelayedCombatDamageDraw(
                entry.getControllerId(), entry.getCard(), drawEffect.sourcePredicate(),
                drawEffect.includesPlaneswalkers()));
    }
}
