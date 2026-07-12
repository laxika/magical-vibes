package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;

@CardRegistration(set = "SHM", collectorNumber = "39")
public class GhastlyDiscovery extends Card {

    public GhastlyDiscovery() {
        // Draw two cards, then discard a card.
        // (Conspire is driven by the Scryfall-loaded CONSPIRE keyword and handled by the casting flow.)
        addEffect(EffectSlot.SPELL, new DrawAndDiscardCardEffect(2, 1));
    }
}
