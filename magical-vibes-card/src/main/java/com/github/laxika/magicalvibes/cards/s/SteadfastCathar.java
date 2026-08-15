package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "EMN", collectorNumber = "44")
public class SteadfastCathar extends Card {

    public SteadfastCathar() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(0, 2));
    }
}
