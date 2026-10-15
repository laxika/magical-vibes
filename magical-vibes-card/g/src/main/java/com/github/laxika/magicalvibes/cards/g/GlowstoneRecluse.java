package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "IKO", collectorNumber = "156")
public class GlowstoneRecluse extends Card {

    public GlowstoneRecluse() {
        addEffect(EffectSlot.ON_SELF_MUTATES, new PutCountersOnSourceEffect(1, 1, 2));
    }
}
