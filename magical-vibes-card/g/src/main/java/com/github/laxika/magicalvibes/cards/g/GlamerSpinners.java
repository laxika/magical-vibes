package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachAllAurasToAnotherPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "141")
public class GlamerSpinners extends Card {

    public GlamerSpinners() {
        // Flash and Flying are auto-loaded from the Scryfall keyword registry.
        // When this creature enters, attach all Auras enchanting target permanent to another
        // permanent with the same controller. The recipient is chosen as the trigger resolves.
        target(TargetFilters.permanent()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new AttachAllAurasToAnotherPermanentEffect());
    }
}
