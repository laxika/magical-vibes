package com.github.laxika.magicalvibes.cards.r;

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

@CardRegistration(set = "8ED", collectorNumber = "35")
public class RainOfBlades extends Card {

    private static final PermanentPredicate ATTACKING_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsAttackingPredicate()
    ));

    public RainOfBlades() {
        // Rain of Blades deals 1 damage to each attacking creature.
        addEffect(EffectSlot.SPELL, new DealDamageToEachMatchingPermanentEffect(
                1, ATTACKING_CREATURE, EachPermanentScope.ALL_PLAYERS));
    }
}
