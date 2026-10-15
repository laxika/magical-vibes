package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "M10", collectorNumber = "171")
public class BrambleCreeper extends Card {

    public BrambleCreeper() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(5, 0));
    }
}
