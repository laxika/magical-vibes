package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedTargetGroup;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEndStepTriggerEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegisterDelayedEndStepTriggerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedEndStepTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterDelayedEndStepTriggerEffect) effect;
        Card triggerCard = entry.getCard().createRuntimeCopy();
        triggerCard.clearRuntimeSpellTargets();
        for (DelayedTargetGroup group : registration.targetGroups()) {
            triggerCard.target(group.filter(), group.minTargets(), group.maxTargets());
        }
        triggerCard.registerEffectTargetIndex(registration.triggerEffect(), 0);

        UUID affectedPermanentId = entry.getTargetId() != null
                ? entry.getTargetId()
                : entry.getTriggeringPermanentId() != null
                ? entry.getTriggeringPermanentId() : entry.getSourcePermanentId();
        gameData.queueDelayedAction(new DelayedEndStepTrigger(
                entry.getControllerId(), triggerCard, entry.getSourcePermanentId(),
                affectedPermanentId, registration.triggerEffect(), registration.targetGroups()));
    }
}
