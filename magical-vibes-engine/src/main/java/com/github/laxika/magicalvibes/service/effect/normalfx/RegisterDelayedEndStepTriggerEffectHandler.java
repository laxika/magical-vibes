package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedTargetGroup;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEndStepTriggerEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        if (registration.aboutActivePlayer()) {
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                    entry.getControllerId(), triggerCard,
                    new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY), registration.triggerEffect()),
                    entry.getSourcePermanentId(), null, gameData.activePlayerId, DelayedReferents.NONE));
            return;
        }
        for (DelayedTargetGroup group : registration.targetGroups()) {
            triggerCard.target(group.filter(), group.minTargets(), group.maxTargets());
        }
        if (!registration.targetGroups().isEmpty()) {
            // Without declared groups the effect is unbound, so it sees every affected object
            // ("up to two target planeswalkers ... phase out" is one delayed ability over both).
            triggerCard.registerEffectTargetIndex(registration.triggerEffect(), 0);
        }

        if (registration.targetGroups().isEmpty()) {
            List<UUID> affectedPermanentIds = entry.targetsForEffect(registration);
            if (affectedPermanentIds.isEmpty() && !entry.getTargetIds().isEmpty()) {
                affectedPermanentIds = entry.getTargetIds();
            }
            if (!affectedPermanentIds.isEmpty()) {
                queueDelayedTrigger(gameData, entry, triggerCard, affectedPermanentIds, registration);
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
        queueDelayedTrigger(gameData, entry, triggerCard, List.of(affectedPermanentId), registration);
    }

    /**
     * One delayed ability covers all of its chosen objects, so "up to two target ..." is a single trigger. An ability
     * with target groups of its own chooses its targets as it goes on the stack (CR 603.3d), so the objects it is
     * about are only "that creature".
     */
    private void queueDelayedTrigger(GameData gameData, StackEntry entry, Card triggerCard,
                                     List<UUID> affectedPermanentIds,
                                     RegisterDelayedEndStepTriggerEffect registration) {
        DelayedReferents referents = registration.targetGroups().isEmpty()
                ? DelayedReferents.permanents(affectedPermanentIds) : DelayedReferents.NONE;
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), triggerCard,
                new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY), registration.triggerEffect()),
                entry.getSourcePermanentId(), affectedPermanentIds.getFirst(), null, referents));
    }
}
