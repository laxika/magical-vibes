package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "175")
public class FlowstoneSalamander extends Card {

    public FlowstoneSalamander() {
        // {R}: This creature deals 1 damage to target creature blocking it.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "{R}: This creature deals 1 damage to target creature blocking it.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentBlockingSourcePredicate()
                        )),
                        "Target must be a creature blocking this creature"
                )
        ));
    }
}
