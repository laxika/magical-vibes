package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;

@CardRegistration(set = "DOM", collectorNumber = "162")
public class GaeasProtector extends Card {

    public GaeasProtector() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedIfAbleEffect());
    }
}
