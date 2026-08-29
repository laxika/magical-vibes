package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "USG", collectorNumber = "88")
public class PeregrineDrake extends Card {

    public PeregrineDrake() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new UntapPermanentsEffect(TapUntapScope.ALL_PERMANENTS,
                        new PermanentIsLandPredicate(), 5));
    }
}
