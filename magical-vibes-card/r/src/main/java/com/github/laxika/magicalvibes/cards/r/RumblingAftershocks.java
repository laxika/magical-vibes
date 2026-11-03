package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WWK", collectorNumber = "89")
public class RumblingAftershocks extends Card {

    public RumblingAftershocks() {
        addTrigger(Triggers.youCastSpell().kicked(),
                new MayEffect(new DealDamageToAnyTargetEffect(new EventValue()), "Have Rumbling Aftershocks deal damage?"));
    }
}
