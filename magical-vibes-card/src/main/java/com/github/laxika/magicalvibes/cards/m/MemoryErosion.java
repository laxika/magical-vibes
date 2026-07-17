package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "ALA", collectorNumber = "50")
public class MemoryErosion extends Card {

    public MemoryErosion() {
        // Whenever an opponent casts a spell, that player mills two cards.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new MillEffect(2, MillRecipient.TARGET_PLAYER));
    }
}
