package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedNontokenAttackTokenCreation;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedNontokenAttackTokenEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegisterDelayedNontokenAttackTokenEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedNontokenAttackTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RegisterDelayedNontokenAttackTokenEffect delayed = (RegisterDelayedNontokenAttackTokenEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.queueDelayedAction(new DelayedNontokenAttackTokenCreation(
                controllerId, delayed.tokenEffect(), entry.getCard()));
    }
}
