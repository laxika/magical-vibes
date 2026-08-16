package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AKH", collectorNumber = "79")
@CardRegistration(set = "AKR", collectorNumber = "92")
public class BalefulAmmit extends Card {

    public BalefulAmmit() {
        // Lifelink is auto-loaded from Scryfall.
        // "When this creature enters, put a -1/-1 counter on target creature you control."
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
    }
}
