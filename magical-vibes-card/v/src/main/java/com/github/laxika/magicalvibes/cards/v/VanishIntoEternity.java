package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONE", collectorNumber = "36")
public class VanishIntoEternity extends Card {

    public VanishIntoEternity() {
        addStaticEffect(new IncreaseOwnCastCostIfTargetingPermanentEffect(
                new PermanentIsCreaturePredicate(), 3));

        target(TargetFilters.nonlandPermanent()).addSpellEffect(new ExileTargetPermanentEffect());
    }
}
