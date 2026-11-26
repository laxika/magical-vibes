package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class HowlpackAvenger extends Card {

    public HowlpackAvenger() {
        addTrigger(Triggers.permanentYouControlDealtDamage(),
                DealDamageEffect.toAnyTarget(new EventValue()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(BoostEffect.self(2, 0)),
                "{1}{R}: This creature gets +2/+0 until end of turn."));
    }
}
