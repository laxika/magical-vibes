package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedBeginningOfCombatTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedTargetGroup;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedBeginningOfCombatTriggerEffect;
import org.springframework.stereotype.Component;

@Component
public class RegisterDelayedBeginningOfCombatTriggerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedBeginningOfCombatTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterDelayedBeginningOfCombatTriggerEffect) effect;
        Card triggerCard = entry.getCard().createRuntimeCopy();
        triggerCard.clearRuntimeSpellTargets();
        for (DelayedTargetGroup group : registration.targetGroups()) {
            triggerCard.target(group.filter(), group.minTargets(), group.maxTargets());
        }
        triggerCard.registerEffectTargetIndex(registration.triggerEffect(), 0);
        gameData.queueDelayedAction(new DelayedBeginningOfCombatTrigger(
                entry.getControllerId(), triggerCard, registration.triggerEffect()));
    }
}
