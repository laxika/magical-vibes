package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "GTC", collectorNumber = "164")
public class FortressCyclops extends Card {

    public FortressCyclops() {
        // Whenever this creature attacks, it gets +3/+0 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(3, 0));

        // Whenever this creature blocks, it gets +0/+3 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(0, 3));
    }
}
