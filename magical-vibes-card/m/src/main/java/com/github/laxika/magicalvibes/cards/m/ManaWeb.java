package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapLandsThatCouldProduceSameManaAsTappedLandEffect;

@CardRegistration(set = "WTH", collectorNumber = "152")
public class ManaWeb extends Card {

    public ManaWeb() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new TapLandsThatCouldProduceSameManaAsTappedLandEffect());
    }
}
