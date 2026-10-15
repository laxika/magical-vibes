package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "60")
public class Waterwhirl extends Card {

    public Waterwhirl() {
        // Return up to two target creatures to their owners' hands.
        target(TargetFilters.creature(), 0, 2).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
