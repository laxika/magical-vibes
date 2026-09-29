package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "FIC", collectorNumber = "64")
@CardRegistration(set = "FIC", collectorNumber = "157")
public class VincentVengefulAtoner extends Card {

    public VincentVengefulAtoner() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, new PutCountersOnSourceEffect(1, 1, 1), false, true));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ConditionalEffect(new SourcePowerAtLeast(7), new DealDamageToEachOtherOpponentEffect()));
    }
}
