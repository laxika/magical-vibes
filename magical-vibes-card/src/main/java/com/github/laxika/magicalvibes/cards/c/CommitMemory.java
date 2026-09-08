package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.Memory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentIntoLibraryNFromTopEffect;

/**
 * Commit // Memory — front half (Commit).
 * Instant — Put target spell or nonland permanent into its owner's library second from the top.
 * Back half (Memory) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "211")
@CardRegistration(set = "AKR", collectorNumber = "54")
public class CommitMemory extends Card {

    public CommitMemory() {
        setBackFaceCard(new Memory());

        // Put target spell or nonland permanent into its owner's library second from the top.
        addEffect(EffectSlot.SPELL, new PutTargetSpellOrPermanentIntoLibraryNFromTopEffect(1));
    }

    @Override
    public String getBackFaceClassName() {
        return "Memory";
    }
}
