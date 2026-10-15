package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "BRO", collectorNumber = "39")
public class TocasiasOnulet extends Card {

    public TocasiasOnulet() {
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new GainLifeEffect(2));
        addUnearth("{3}{W}");
    }
}
