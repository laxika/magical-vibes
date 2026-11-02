package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "233")
public class SunhomeEnforcer extends Card {

    public SunhomeEnforcer() {
        addTrigger(Triggers.dealsCombatDamage(), new GainLifeEffect(new EventValue()));
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{R}", List.of(new BoostSelfEffect(1, 0)),
                "{1}{R}: This creature gets +1/+0 until end of turn."));
    }
}
