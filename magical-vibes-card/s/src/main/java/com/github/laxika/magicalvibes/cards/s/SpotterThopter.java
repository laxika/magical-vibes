package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "BRO", collectorNumber = "80")
public class SpotterThopter extends Card {

    public SpotterThopter() {
        addPrototype("{3}{U}", CardColor.BLUE, 2, 3);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(new SourcePower()));
    }
}
