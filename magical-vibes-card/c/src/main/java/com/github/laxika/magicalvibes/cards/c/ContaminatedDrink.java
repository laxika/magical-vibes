package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveControllerRadCountersEffect;

@CardRegistration(set = "PIP", collectorNumber = "99")
@CardRegistration(set = "PIP", collectorNumber = "627")
public class ContaminatedDrink extends Card {

    public ContaminatedDrink() {
        addSpellEffect(new DrawCardEffect(new XValue()));
        addSpellEffect(new GiveControllerRadCountersEffect(new HalvedRoundedUp(new XValue())));
    }
}
