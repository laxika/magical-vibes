package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "JOU", collectorNumber = "83")
public class SpitefulBlow extends Card {

    public SpitefulBlow() {
        setAllowSharedTargets(true);

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature"
        ));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Second target must be a land"
        ));

        addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
