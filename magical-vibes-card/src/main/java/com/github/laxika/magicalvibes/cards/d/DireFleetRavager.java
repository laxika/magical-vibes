package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;

@CardRegistration(set = "XLN", collectorNumber = "104")
@CardRegistration(set = "OTC", collectorNumber = "132")
public class DireFleetRavager extends Card {

    public DireFleetRavager() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EachPlayerLosesFractionOfLifeRoundedUpEffect(3));
    }
}
