package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "AKH", collectorNumber = "53")
@CardRegistration(set = "AKR", collectorNumber = "62")
public class Floodwaters extends Card {

    public Floodwaters() {
        // Return up to two target creatures to their owners' hands.
        target(TargetFilters.creature(), 0, 2).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
