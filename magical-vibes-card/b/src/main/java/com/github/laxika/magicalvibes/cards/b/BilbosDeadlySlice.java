package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOB", collectorNumber = "62")
public class BilbosDeadlySlice extends Card {

    public BilbosDeadlySlice() {
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect());
    }
}
