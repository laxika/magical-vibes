package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ECL", collectorNumber = "68")
public class Shinestriker extends Card {

    public Shinestriker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DrawCardEffect(new ColorsAmongControlledPermanents()));
    }
}
