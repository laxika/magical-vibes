package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "33")
public class PortableHole extends Card {

    private static final PermanentAllOfPredicate TARGET_PREDICATE = new PermanentAllOfPredicate(List.of(
            new PermanentNotPredicate(new PermanentIsLandPredicate()),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
            new PermanentMaxManaValuePredicate(2)
    ));

    public PortableHole() {
        target(new PermanentPredicateTargetFilter(
                TARGET_PREDICATE,
                "Target must be a nonland permanent an opponent controls with mana value 2 or less"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect(false, TARGET_PREDICATE));
    }
}
