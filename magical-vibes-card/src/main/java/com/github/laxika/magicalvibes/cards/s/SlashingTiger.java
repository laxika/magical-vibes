package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "PTK", collectorNumber = "145")
public class SlashingTiger extends Card {

    public SlashingTiger() {
        // Whenever this creature becomes blocked, it gets +2/+2 until end of turn.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(2, 2));
    }
}
