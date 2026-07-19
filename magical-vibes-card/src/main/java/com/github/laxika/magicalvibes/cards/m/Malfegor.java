package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenEachOpponentSacrificesCreaturePerCardEffect;

@CardRegistration(set = "CON", collectorNumber = "117")
public class Malfegor extends Card {

    public Malfegor() {
        // Flying is auto-loaded from Scryfall.

        // When Malfegor enters, discard your hand. Each opponent sacrifices a creature of their
        // choice for each card discarded this way.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardOwnHandThenEachOpponentSacrificesCreaturePerCardEffect());
    }
}
