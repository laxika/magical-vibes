package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "USG", collectorNumber = "272")
public class Retaliation extends Card {

    public Retaliation() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_BLOCKED, new BoostSelfEffect(1, 1));
    }
}
