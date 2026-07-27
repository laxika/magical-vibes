package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DKA", collectorNumber = "15")
public class NiblisOfTheMist extends Card {

    public NiblisOfTheMist() {
        // Flying is loaded from Scryfall.
        // When this creature enters, you may tap target creature.
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                "Tap target creature?"
        ));
    }
}
