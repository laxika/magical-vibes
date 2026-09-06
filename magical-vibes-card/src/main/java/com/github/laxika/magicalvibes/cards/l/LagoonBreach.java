package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

public class LagoonBreach extends Card {

    public LagoonBreach() {
        PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        target(new PermanentPredicateTargetFilter(
                        targetPredicate, "Target must be an attacking creature you don't control"))
                .addEffect(EffectSlot.SPELL,
                        new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0, targetPredicate));
    }
}
