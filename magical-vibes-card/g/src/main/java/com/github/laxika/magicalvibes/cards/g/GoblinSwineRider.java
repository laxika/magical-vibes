package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "81")
public class GoblinSwineRider extends Card {

    private static final PermanentPredicate ATTACKING_OR_BLOCKING_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentAnyOfPredicate(List.of(
                    new PermanentIsAttackingPredicate(),
                    new PermanentIsBlockingPredicate()
            ))
    ));

    public GoblinSwineRider() {
        // Whenever this creature becomes blocked, it deals 2 damage to each attacking
        // creature and each blocking creature.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DealDamageToEachMatchingPermanentEffect(
                2, ATTACKING_OR_BLOCKING_CREATURE, EachPermanentScope.ALL_PLAYERS));
    }
}
