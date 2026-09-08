package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "DSK", collectorNumber = "70")
public class PiranhaFly extends Card {

    public PiranhaFly() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
