package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "157")
public class TinWingChimera extends Card {

    public TinWingChimera() {
        // Sacrifice this creature: Put a +2/+2 counter on target Chimera creature.
        // It gains flying. (This effect lasts indefinitely.)
        PermanentPredicate chimeraCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.CHIMERA)));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new PutCountersEffect(CounterType.PLUS_TWO_PLUS_TWO, 1),
                        new GrantKeywordEffect(Keyword.FLYING, Subjects.target(), Duration.PERMANENT)),
                "Sacrifice this creature: Put a +2/+2 counter on target Chimera creature. It gains flying.",
                new PermanentPredicateTargetFilter(chimeraCreature, "Target must be a Chimera creature")));
    }
}
