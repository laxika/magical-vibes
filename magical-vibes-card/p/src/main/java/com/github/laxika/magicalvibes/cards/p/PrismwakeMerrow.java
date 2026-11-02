package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "46")
public class PrismwakeMerrow extends Card {

    public PrismwakeMerrow() {
        // Flash is auto-loaded from the Scryfall keyword registry.
        // When this creature enters, target permanent becomes the color or colors of your choice
        // until end of turn. The controller picks the colors as the ETB trigger resolves.
        target(TargetFilters.permanent()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new BecomeChosenColorsUntilEndOfTurnEffect());
    }
}
