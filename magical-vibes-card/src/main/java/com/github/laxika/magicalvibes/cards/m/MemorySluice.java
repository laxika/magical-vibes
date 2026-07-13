package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "SHM", collectorNumber = "170")
public class MemorySluice extends Card {

    public MemorySluice() {
        // Target player mills four cards.
        // (Conspire is driven by the Scryfall-loaded CONSPIRE keyword and handled by the casting flow.)
        addEffect(EffectSlot.SPELL, new MillEffect(4, MillRecipient.TARGET_PLAYER));
    }
}
