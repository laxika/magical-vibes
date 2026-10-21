package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "174")
public class TitanothRex extends Card {

    public TitanothRex() {
        // When you cycle this card, put a trample counter on target creature you control.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_SELF_CYCLED, new PutCounterOnTargetPermanentEffect(CounterType.TRAMPLE, 1));

        // Cycling {1}{G} ({1}{G}, Discard this card: Draw a card.)
        addCycling("{1}{G}");
    }
}
