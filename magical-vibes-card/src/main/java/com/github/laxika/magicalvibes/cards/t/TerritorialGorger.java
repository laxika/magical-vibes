package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "KLD", collectorNumber = "136")
public class TerritorialGorger extends Card {

    public TerritorialGorger() {
        addEffect(EffectSlot.ON_CONTROLLER_GETS_ENERGY, new BoostSelfEffect(2, 2));
    }
}
