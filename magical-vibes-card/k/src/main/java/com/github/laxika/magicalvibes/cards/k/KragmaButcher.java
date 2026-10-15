package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "BNG", collectorNumber = "100")
public class KragmaButcher extends Card {

    public KragmaButcher() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new BoostSelfEffect(2, 0));
    }
}
