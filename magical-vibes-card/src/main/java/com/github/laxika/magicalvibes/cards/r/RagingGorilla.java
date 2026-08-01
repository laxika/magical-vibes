package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "VIS", collectorNumber = "90")
public class RagingGorilla extends Card {

    public RagingGorilla() {
        // Whenever this creature blocks or becomes blocked, it gets +2/-2 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(2, -2));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(2, -2));
    }
}
