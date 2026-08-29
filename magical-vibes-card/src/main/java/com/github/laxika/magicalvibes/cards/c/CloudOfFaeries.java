package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ULG", collectorNumber = "29")
public class CloudOfFaeries extends Card {

    public CloudOfFaeries() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new UntapPermanentsEffect(TapUntapScope.ALL_PERMANENTS,
                        new PermanentIsLandPredicate(), 2));
        addCycling("{2}");
    }
}
