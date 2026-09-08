package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromControlledPermanentsToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "RNA", collectorNumber = "175")
public class GallopingLizrog extends Card {

    public GallopingLizrog() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MoveCountersFromControlledPermanentsToSourceEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, new PermanentIsCreaturePredicate(), 2, true));
    }
}
