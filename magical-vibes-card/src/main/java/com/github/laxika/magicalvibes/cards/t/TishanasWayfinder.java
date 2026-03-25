package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

@CardRegistration(set = "XLN", collectorNumber = "211")
public class TishanasWayfinder extends Card {

    public TishanasWayfinder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExploreEffect());
    }
}
