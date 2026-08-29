package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "39")
public class BuryInBooks extends Card {

    public BuryInBooks() {
        PermanentPredicate attackingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingPermanentEffect(attackingCreature, 2));
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature"))
                .addEffect(EffectSlot.SPELL, new PutTargetPermanentIntoLibraryNFromTopEffect(1));
    }
}
