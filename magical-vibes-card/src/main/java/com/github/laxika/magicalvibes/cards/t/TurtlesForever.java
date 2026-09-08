package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TurtlesForeverEffect;

@CardRegistration(set = "TMT", collectorNumber = "27")
@CardRegistration(set = "TMT", collectorNumber = "261")
public class TurtlesForever extends Card {

    public TurtlesForever() {
        addEffect(EffectSlot.SPELL, new TurtlesForeverEffect());
    }
}
