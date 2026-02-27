package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SOM", collectorNumber = "44")
public class SkyEelSchool extends Card {

    public SkyEelSchool() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardCardEffect());
    }
}
