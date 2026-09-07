package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

@CardRegistration(set = "LCI", collectorNumber = "206")
public class PathfindingAxejaw extends Card {

    public PathfindingAxejaw() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExploreEffect());
    }
}
