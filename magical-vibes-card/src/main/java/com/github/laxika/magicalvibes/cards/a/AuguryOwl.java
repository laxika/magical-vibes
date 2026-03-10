package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "45")
public class AuguryOwl extends Card {

    public AuguryOwl() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(3));
    }
}
