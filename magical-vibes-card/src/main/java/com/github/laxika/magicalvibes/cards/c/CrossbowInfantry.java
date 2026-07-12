package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "12")
@CardRegistration(set = "8ED", collectorNumber = "15")
public class CrossbowInfantry extends Card {

    public CrossbowInfantry() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "{T}: This creature deals 1 damage to target attacking or blocking creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentIsBlockingPredicate()
                        )),
                        "Target must be an attacking or blocking creature"
                )));
    }
}
