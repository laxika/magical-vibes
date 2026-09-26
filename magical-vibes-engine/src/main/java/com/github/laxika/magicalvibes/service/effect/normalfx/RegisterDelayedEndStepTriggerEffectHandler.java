package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedTargetGroup;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEndStepTriggerEffect;
import org.springframework.stereotype.Component;

import java.util.List;
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

        if (registration.targetGroups().isEmpty()) {
            List<UUID> affectedPermanentIds = entry.targetsForEffect(registration);
            if (!affectedPermanentIds.isEmpty()) {
                for (UUID affectedPermanentId : affectedPermanentIds) {
                    queueDelayedTrigger(gameData, entry, triggerCard, affectedPermanentId, registration);
                }
                return;
            }
            if (entry.getTargetId() == null
                    && registration.triggerEffect().targetSpec().declaredTarget() != null) {
                return;
            }
        }

        UUID affectedPermanentId = entry.getTargetId() != null
                ? entry.getTargetId()
                : entry.getTriggeringPermanentId() != null
                ? entry.getTriggeringPermanentId() : entry.getSourcePermanentId();
        queueDelayedTrigger(gameData, entry, triggerCard, affectedPermanentId, registration);
    }

    private void queueDelayedTrigger(GameData gameData, StackEntry entry, Card triggerCard,
                                     UUID affectedPermanentId,
                                     RegisterDelayedEndStepTriggerEffect registration) {
        gameData.queueDelayedAction(new DelayedEndStepTrigger(
                entry.getControllerId(), triggerCard, entry.getSourcePermanentId(),
                affectedPermanentId, registration.triggerEffect(), registration.targetGroups()));
    }
}
