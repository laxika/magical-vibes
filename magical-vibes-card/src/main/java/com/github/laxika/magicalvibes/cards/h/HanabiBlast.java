package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnThisSpellToHandThenDiscardAtRandomEffect;

@CardRegistration(set = "CHK", collectorNumber = "170")
public class HanabiBlast extends Card {

    public HanabiBlast() {
        // Hanabi Blast deals 2 damage to any target. Return Hanabi Blast to its owner's hand,
        // then discard a card at random.
        //
        // The bounce precedes the discard, so the spell itself can be the card discarded at random.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
        addEffect(EffectSlot.SPELL, new ReturnThisSpellToHandThenDiscardAtRandomEffect());
    }
}
