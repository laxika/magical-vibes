package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "DMU", collectorNumber = "100")
public class PhyrexianVivisector extends Card {

    public PhyrexianVivisector() {
        addEffect(EffectSlot.ON_DEATH, new ScryEffect(1));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new ScryEffect(1));
    }
}
