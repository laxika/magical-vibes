package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DGM", collectorNumber = "105")
public class SpeciesGorger extends Card {

    public SpeciesGorger() {
        // At the beginning of your upkeep, return a creature you control to its owner's hand.
        // Mandatory non-targeting choice at resolution; may (and alone, must) return itself.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentIsCreaturePredicate(),
                "creature"
        ));
    }
}
