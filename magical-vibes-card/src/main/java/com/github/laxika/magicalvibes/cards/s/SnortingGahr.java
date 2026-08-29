package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "MMQ", collectorNumber = "272")
public class SnortingGahr extends Card {

    public SnortingGahr() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(2, 2));
    }
}
