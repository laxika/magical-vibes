package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ISD", collectorNumber = "150")
public class IntoTheMawOfHell extends Card {

    public IntoTheMawOfHell() {
        setAllowSharedTargets(true);

        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "First target must be a land"
        )).addSpellEffect(new DestroyTargetPermanentEffect());

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Second target must be a creature"
        )).addSpellEffect(new DealDamageToTargetCreatureEffect(13));
    }
}
