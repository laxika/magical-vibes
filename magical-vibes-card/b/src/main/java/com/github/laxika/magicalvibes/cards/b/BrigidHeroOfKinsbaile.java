package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "6")
public class BrigidHeroOfKinsbaile extends Card {

    private static final PermanentPredicate ATTACKING_OR_BLOCKING_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentAnyOfPredicate(List.of(
                    new PermanentIsAttackingPredicate(),
                    new PermanentIsBlockingPredicate()
            ))
    ));

    public BrigidHeroOfKinsbaile() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToEachMatchingPermanentEffect(
                        2, ATTACKING_OR_BLOCKING_CREATURE, EachPermanentScope.TARGET_PLAYER)),
                "{T}: Brigid deals 2 damage to each attacking or blocking creature target player controls."));
    }
}
