package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoMonarch;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForMonarchEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

@CardRegistration(set = "LTC", collectorNumber = "18")
@CardRegistration(set = "LTC", collectorNumber = "101")
public class ArchivistOfGondor extends Card {

    public ArchivistOfGondor() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCommanderPredicate(),
                        new ConditionalEffect(new NoMonarch(), new BecomeMonarchEffect())));

        addEffect(EffectSlot.END_STEP_TRIGGERED, new DrawCardForMonarchEndStepEffect());
    }
}
