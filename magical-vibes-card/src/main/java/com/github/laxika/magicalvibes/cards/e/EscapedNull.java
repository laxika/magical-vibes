package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "ROE", collectorNumber = "109")
public class EscapedNull extends Card {

    public EscapedNull() {
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(5, 0));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(5, 0));
    }
}
