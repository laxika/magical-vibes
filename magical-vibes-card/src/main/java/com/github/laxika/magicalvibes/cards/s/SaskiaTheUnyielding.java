package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToChosenPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SLC", collectorNumber = "55")
public class SaskiaTheUnyielding extends Card {

    public SaskiaTheUnyielding() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChoosePlayerOnEnterEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        new DealDamageToChosenPlayerEffect(new EventValue())));
    }
}
