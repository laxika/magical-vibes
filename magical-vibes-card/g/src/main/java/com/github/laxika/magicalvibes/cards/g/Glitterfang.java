package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "SOK", collectorNumber = "100")
public class Glitterfang extends Card {

    public Glitterfang() {
        // Haste is auto-loaded from Scryfall.
        // At the beginning of the end step, return this creature to its owner's hand.
        addEffect(EffectSlot.END_STEP_TRIGGERED, ReturnToHandEffect.self());
    }
}
