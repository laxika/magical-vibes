package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageToken;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageTokenEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RegisterDelayedCombatDamageTokenEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCombatDamageTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var delayedEffect = (RegisterDelayedCombatDamageTokenEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetIds() != null) {
            targetIds = entry.getTargetIds();
        }
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty()) {
            return;
        }

        gameData.queueDelayedAction(new DelayedCombatDamageToken(
                entry.getControllerId(), targetIds.getFirst(), delayedEffect.tokenEffect(), entry.getCard()));
    }
}
