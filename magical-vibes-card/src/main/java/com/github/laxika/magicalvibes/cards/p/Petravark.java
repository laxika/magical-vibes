package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TOR", collectorNumber = "109")
public class Petravark extends Card {

    public Petravark() {
        target(TargetFilters.land()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetPermanentUntilSourceLeavesEffect(false, new PermanentIsLandPredicate()));
    }
}
