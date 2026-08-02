package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "M15", collectorNumber = "181")
public class InvasiveSpecies extends Card {

    public InvasiveSpecies() {
        // When this creature enters, return another permanent you control to its owner's hand.
        // Mandatory and non-targeting: the permanent is chosen at resolution among the controller's
        // other permanents; "another" excludes the Species itself.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                "permanent"
        ));
    }
}
