package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ZEN", collectorNumber = "163")
public class GrazingGladehart extends Card {

    public GrazingGladehart() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MayEffect(new GainLifeEffect(2), "Gain 2 life?"));
    }
}
