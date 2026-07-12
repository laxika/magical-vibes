package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachAllAurasToAnotherPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SHM", collectorNumber = "141")
public class GlamerSpinners extends Card {

    public GlamerSpinners() {
        // Flash and Flying are auto-loaded from the Scryfall keyword registry.
        // When this creature enters, attach all Auras enchanting target permanent to another
        // permanent with the same controller. The recipient is chosen as the trigger resolves.
        target(new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AttachAllAurasToAnotherPermanentEffect());
    }
}
