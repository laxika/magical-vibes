package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SHM", collectorNumber = "228")
public class Heartmender extends Card {

    public Heartmender() {
        // At the beginning of your upkeep, remove a -1/-1 counter from each creature you control.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new RemoveCounterFromEachControlledPermanentEffect(
                CounterType.MINUS_ONE_MINUS_ONE, 1, new PermanentIsCreaturePredicate()));

        // Persist is a keyword loaded from Scryfall; the engine handles the return automatically.
    }
}
