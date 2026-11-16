package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedTargetGroup;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedBeginningOfCombatTriggerEffect;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStepThisTurn(
                Triggers.beginningOfCombat(ActivePlayer.ANY), entry.getControllerId(), triggerCard,
                entry.getSourcePermanentId(), null, DelayedReferents.NONE, registration.triggerEffect()));
    }
}
