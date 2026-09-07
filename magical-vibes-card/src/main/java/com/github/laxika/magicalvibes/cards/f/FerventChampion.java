package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivatedAbilityCostForTargetingSourceEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "124")
public class FerventChampion extends Card {

    private static final PermanentPredicate ANOTHER_ATTACKING_KNIGHT = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.KNIGHT),
            new PermanentIsAttackingPredicate(),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
    ));

    public FerventChampion() {
        target(new ControlledPermanentPredicateTargetFilter(
                ANOTHER_ATTACKING_KNIGHT,
                "Target must be another attacking Knight you control"
        )).addEffect(EffectSlot.ON_ATTACK, new BoostTargetCreatureEffect(1, 0, ANOTHER_ATTACKING_KNIGHT));

        addEffect(EffectSlot.STATIC, new ReduceActivatedAbilityCostForTargetingSourceEffect(3));
    }
}
