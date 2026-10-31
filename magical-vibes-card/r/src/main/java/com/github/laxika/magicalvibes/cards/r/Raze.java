package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "USG", collectorNumber = "207")
public class Raze extends Card {

    public Raze() {
        addCastingCost(new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"));
        target(TargetFilters.land()).addSpellEffect(new DestroyTargetPermanentEffect());
    }
}
