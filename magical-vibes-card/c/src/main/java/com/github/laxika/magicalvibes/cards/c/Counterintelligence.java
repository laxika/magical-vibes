package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PTK", collectorNumber = "41")
public class Counterintelligence extends Card {

    public Counterintelligence() {
        // Return one or two target creatures to their owners' hands.
        target(TargetFilters.creature(), 1, 2)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
