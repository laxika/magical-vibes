package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "8ED", collectorNumber = "272")
public class PlowUnder extends Card {

    public PlowUnder() {
        // Put two target lands on top of their owners' libraries. Each target is its own group,
        // so the single-target PutTargetOnTopOfLibraryEffect handler processes both, and the two
        // targets are enforced to be distinct lands.
        PermanentPredicateTargetFilter landFilter = new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        );
        target(landFilter).addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect());
        target(landFilter).addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect());
    }
}
