package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "ECL", collectorNumber = "160")
public class Squawkroaster extends Card {

    public Squawkroaster() {
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(new ColorsAmongControlledPermanents(), new Fixed(4)));
    }
}
