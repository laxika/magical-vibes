package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;

@CardRegistration(set = "RAV", collectorNumber = "169")
@CardRegistration(set = "DDJ", collectorNumber = "59")
@CardRegistration(set = "MMA", collectorNumber = "146")
public class GreaterMossdog extends Card {

    public GreaterMossdog() {
        addEffect(EffectSlot.GRAVEYARD_DRAW_REPLACEMENT, new DredgeEffect(3));
    }
}
