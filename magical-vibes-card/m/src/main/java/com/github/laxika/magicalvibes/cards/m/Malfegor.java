package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenEachOpponentSacrificesCreaturePerCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CON", collectorNumber = "117")
@CardRegistration(set = "IMA", collectorNumber = "205")
@CardRegistration(set = "CMD", collectorNumber = "208")
@CardRegistration(set = "CMA", collectorNumber = "184")
public class Malfegor extends Card {

    public Malfegor() {
        // Flying is auto-loaded from Scryfall.

        // When Malfegor enters, discard your hand. Each opponent sacrifices a creature of their
        // choice for each card discarded this way.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DiscardOwnHandThenEachOpponentSacrificesCreaturePerCardEffect());
    }
}
