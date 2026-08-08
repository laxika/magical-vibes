package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "DGM", collectorNumber = "105")
public class SpeciesGorger extends Card {

    public SpeciesGorger() {
        // At the beginning of your upkeep, return a creature you control to its owner's hand.
        // Mandatory non-targeting choice at resolution; may (and alone, must) return itself.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentIsCreaturePredicate(),
                "creature"
        ));
    }
}
