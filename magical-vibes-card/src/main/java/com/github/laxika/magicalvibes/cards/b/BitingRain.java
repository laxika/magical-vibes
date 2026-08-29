package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "SOI", collectorNumber = "102")
public class BitingRain extends Card {

    public BitingRain() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, -2));
        addCastingOption(new MadnessCast("{2}{B}"));
    }
}
