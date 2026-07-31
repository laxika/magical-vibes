package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.Comply;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellToHandEffect;

/**
 * Failure // Comply — front half (Failure).
 * Instant — Return target spell to its owner's hand.
 * Back half (Comply) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "221")
public class FailureComply extends Card {

    public FailureComply() {
        setBackFaceCard(new Comply());

        // Return target spell to its owner's hand.
        // SPELL_ON_STACK targetSpec on the effect auto-derives "any spell on the stack".
        addEffect(EffectSlot.SPELL, new ReturnTargetSpellToHandEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "Comply";
    }
}
