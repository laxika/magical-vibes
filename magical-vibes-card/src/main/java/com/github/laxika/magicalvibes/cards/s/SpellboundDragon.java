package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardBoostSelfByDiscardedManaValueEffect;

@CardRegistration(set = "ARB", collectorNumber = "90")
public class SpellboundDragon extends Card {

    public SpellboundDragon() {
        // Flying is loaded from Scryfall metadata.
        // Whenever this creature attacks, draw a card, then discard a card. This creature gets
        // +X/+0 until end of turn, where X is the discarded card's mana value.
        addEffect(EffectSlot.ON_ATTACK, new DrawDiscardBoostSelfByDiscardedManaValueEffect());
    }
}
