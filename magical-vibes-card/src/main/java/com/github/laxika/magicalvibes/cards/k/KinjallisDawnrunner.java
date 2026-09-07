package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

@CardRegistration(set = "LCI", collectorNumber = "19")
public class KinjallisDawnrunner extends Card {

    public KinjallisDawnrunner() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExploreEffect());
    }
}
