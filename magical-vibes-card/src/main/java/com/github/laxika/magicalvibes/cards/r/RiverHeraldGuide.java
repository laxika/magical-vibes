package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

@CardRegistration(set = "LCI", collectorNumber = "209")
public class RiverHeraldGuide extends Card {

    public RiverHeraldGuide() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExploreEffect());
    }
}
