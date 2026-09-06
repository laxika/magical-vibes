package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;

@CardRegistration(set = "DTK", collectorNumber = "200")
public class SandsteppeScavenger extends Card {

    public SandsteppeScavenger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BolsterEffect(2));
    }
}
