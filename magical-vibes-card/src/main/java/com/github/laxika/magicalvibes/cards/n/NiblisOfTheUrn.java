package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DKA", collectorNumber = "16")
@CardRegistration(set = "INR", collectorNumber = "35")
public class NiblisOfTheUrn extends Card {

    public NiblisOfTheUrn() {
        // Flying is loaded from Scryfall.
        // Whenever this creature attacks, you may tap target creature.
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                "Tap target creature?"
        ));
    }
}
