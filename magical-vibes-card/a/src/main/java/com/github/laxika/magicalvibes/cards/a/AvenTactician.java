package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;

@CardRegistration(set = "DTK", collectorNumber = "6")
public class AvenTactician extends Card {

    public AvenTactician() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BolsterEffect(1));
    }
}
