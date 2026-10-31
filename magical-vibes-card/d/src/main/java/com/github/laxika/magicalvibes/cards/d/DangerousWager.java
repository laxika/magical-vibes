package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

/**
 * Dangerous Wager — {1}{R} Instant
 *
 * Discard your hand, then draw two cards.
 */
@CardRegistration(set = "AVR", collectorNumber = "131")
public class DangerousWager extends Card {

    public DangerousWager() {
        // Discard your hand, then draw two cards (order matters — the drawn cards are kept)
        addSpellEffect(new DiscardHandEffect());
        addSpellEffect(new DrawCardEffect(2));
    }
}
