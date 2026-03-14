package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;

@CardRegistration(set = "M10", collectorNumber = "199")
@CardRegistration(set = "M11", collectorNumber = "193")
public class PrizedUnicorn extends Card {

    public PrizedUnicorn() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect());
    }
}
