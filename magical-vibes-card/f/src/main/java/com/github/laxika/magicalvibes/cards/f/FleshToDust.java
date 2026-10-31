package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M15", collectorNumber = "98")
public class FleshToDust extends Card {

    public FleshToDust() {
        // Destroy target creature. It can't be regenerated.
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect(true));
    }
}
