package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDM", collectorNumber = "79")
public class DragonsPrey extends Card {

    public DragonsPrey() {
        addStaticEffect(ReduceOwnCastCostIfTargetingPermanentEffect.increaseIfTargeting(
                new PermanentHasSubtypePredicate(CardSubtype.DRAGON), 2));
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect());
    }
}
