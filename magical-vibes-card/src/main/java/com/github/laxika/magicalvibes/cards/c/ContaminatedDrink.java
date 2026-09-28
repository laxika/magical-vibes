package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveControllerRadCountersEffect;

@CardRegistration(set = "PIP", collectorNumber = "99")
@CardRegistration(set = "PIP", collectorNumber = "627")
public class ContaminatedDrink extends Card {

    public ContaminatedDrink() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new XValue()));
        addEffect(EffectSlot.SPELL,
                new GiveControllerRadCountersEffect(new HalvedRoundedUp(new XValue())));
    }
}
