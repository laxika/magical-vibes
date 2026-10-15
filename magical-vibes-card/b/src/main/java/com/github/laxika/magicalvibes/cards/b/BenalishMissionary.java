package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "10")
public class BenalishMissionary extends Card {

    public BenalishMissionary() {
        // {1}{W}, {T}: Prevent all combat damage that would be dealt by target blocked creature this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(PreventDamageEffect.allCombatByTargetCreatures()),
                "{1}{W}, {T}: Prevent all combat damage that would be dealt by target blocked creature this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsBlockedPredicate()
                        )),
                        "Target must be a blocked creature"
                )
        ));
    }
}
