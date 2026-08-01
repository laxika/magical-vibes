package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "P02", collectorNumber = "59")
@CardRegistration(set = "VIS", collectorNumber = "47")
public class Undo extends Card {

    public Undo() {
        // Return two target creatures to their owners' hands.
        target(TargetFilters.creature(), 2, 2)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
