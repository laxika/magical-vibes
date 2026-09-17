package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellToHandEffect;

@CardRegistration(set = "MAR", collectorNumber = "5")
public class Reprieve extends Card {

    public Reprieve() {
        // Return target spell to its owner's hand.
        addEffect(EffectSlot.SPELL, new ReturnTargetSpellToHandEffect());

        // Draw a card.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
