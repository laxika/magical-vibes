package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "BRO", collectorNumber = "197")
public class BoulderbranchGolem extends Card {

    public BoulderbranchGolem() {
        addPrototype("{3}{G}", CardColor.GREEN, 3, 3);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(new SourcePower()));
    }
}
