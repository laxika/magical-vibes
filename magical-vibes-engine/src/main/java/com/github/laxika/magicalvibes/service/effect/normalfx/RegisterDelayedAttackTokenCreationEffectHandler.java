package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedAttackTokenCreation;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedAttackTokenCreationEffect;
import org.springframework.stereotype.Component;

@Component
public class RegisterDelayedAttackTokenCreationEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedAttackTokenCreationEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RegisterDelayedAttackTokenCreationEffect registration =
                (RegisterDelayedAttackTokenCreationEffect) effect;
        gameData.queueDelayedAction(new DelayedAttackTokenCreation(
                entry.getControllerId(), registration.amount(), registration.tokenEffect(),
                registration.sacrificeAtEndStep(), entry.getCard()));
    }
}
