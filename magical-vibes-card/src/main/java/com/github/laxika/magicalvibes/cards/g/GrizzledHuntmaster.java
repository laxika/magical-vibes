package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrizzledHuntmasterEffect;

@CardRegistration(set = "YMID", collectorNumber = "49")
public class GrizzledHuntmaster extends Card {

    public GrizzledHuntmaster() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrizzledHuntmasterEffect());
    }
}
