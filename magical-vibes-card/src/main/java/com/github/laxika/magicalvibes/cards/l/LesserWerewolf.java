package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "110")
public class LesserWerewolf extends Card {

    public LesserWerewolf() {
        PermanentAllOfPredicate creatureInCombatWithThis = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentInCombatWithSourcePredicate()
        ));
        CardEffect effect = new ConditionalEffect(
                new SourcePowerAtLeast(1),
                SequenceEffect.of(
                        new BoostSelfEffect(-1, 0),
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ZERO_MINUS_ONE)
                ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(effect),
                "{B}: If this creature's power is 1 or more, it gets -1/-0 until end of turn and put "
                        + "a -0/-1 counter on target creature blocking or blocked by this creature. "
                        + "Activate only during the declare blockers step.",
                new PermanentPredicateTargetFilter(
                        creatureInCombatWithThis,
                        "Target must be a creature blocking or blocked by this creature"
                ),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_DECLARE_BLOCKERS
        ));
    }
}
