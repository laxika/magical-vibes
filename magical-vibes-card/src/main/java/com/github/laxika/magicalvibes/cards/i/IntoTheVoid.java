package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "62")
public class IntoTheVoid extends Card {

    public IntoTheVoid() {
        // Return up to two target creatures to their owners' hands.
        target(TargetFilters.creature(), 0, 2).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
