package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMT", collectorNumber = "7")
public class GroundedForLife extends Card {

    public GroundedForLife() {
        addStaticEffect(new ReduceOwnCastCostIfTargetingPermanentEffect(
                new PermanentIsTappedPredicate(), 3));
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect());
    }
}
