package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUnblockedAttackerUntapRemoveFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.springframework.stereotype.Component;

/** Registers "whenever a creature attacks and isn't blocked this combat, untap it and remove it from combat". */
@Component
public class RegisterDelayedUnblockedAttackerUntapRemoveFromCombatEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedUnblockedAttackerUntapRemoveFromCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.thisTurn(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.creatureAttacksUnblocked(),
                        new UntapPermanentsEffect(Subjects.triggering()),
                        RemoveTargetFromCombatEffect.ofTriggeringPermanent()),
                entry.getSourcePermanentId())
                .withReferents(DelayedReferents.NONE.duringCombatPhase(gameData.combatPhasesThisTurn)));
    }
}
