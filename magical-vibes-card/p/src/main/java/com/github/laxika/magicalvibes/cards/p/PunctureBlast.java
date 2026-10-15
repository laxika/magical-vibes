package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "EVE", collectorNumber = "60")
public class PunctureBlast extends Card {

    public PunctureBlast() {
        // Wither is auto-loaded from Scryfall; the engine deals creature damage as -1/-1 counters.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
