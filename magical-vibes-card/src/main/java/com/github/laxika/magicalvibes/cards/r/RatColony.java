package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOtherControlledSubtypeEffect;

@CardRegistration(set = "DOM", collectorNumber = "101")
public class RatColony extends Card {

    public RatColony() {
        addEffect(EffectSlot.STATIC, new BoostSelfPerOtherControlledSubtypeEffect(CardSubtype.RAT, 1, 0));
    }
}
