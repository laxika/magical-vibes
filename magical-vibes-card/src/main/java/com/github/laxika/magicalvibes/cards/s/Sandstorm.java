package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "269")
public class Sandstorm extends Card {

    private static final PermanentPredicate ATTACKING_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsAttackingPredicate()
    ));

    public Sandstorm() {
        // Sandstorm deals 1 damage to each attacking creature.
        addEffect(EffectSlot.SPELL, new DealDamageToEachMatchingPermanentEffect(
                1, ATTACKING_CREATURE, EachPermanentScope.ALL_PLAYERS));
    }
}
