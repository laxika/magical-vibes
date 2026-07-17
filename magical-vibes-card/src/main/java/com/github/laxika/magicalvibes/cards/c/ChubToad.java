package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "5ED", collectorNumber = "283")
public class ChubToad extends Card {

    public ChubToad() {
        // Whenever this creature blocks or becomes blocked, it gets +2/+2 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(2, 2));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(2, 2));
    }
}
