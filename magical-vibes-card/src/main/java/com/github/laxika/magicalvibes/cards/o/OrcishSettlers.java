package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "112")
public class OrcishSettlers extends Card {

    public OrcishSettlers() {
        // {X}{X}{R}, {T}, Sacrifice this creature: Destroy X target lands. The double {X} makes the
        // generic portion cost twice the chosen X; the target count still scales with X itself.
        addActivatedAbility(new ActivatedAbility(true, "{X}{X}{R}",
                List.of(new SacrificeSelfCost(), new DestroyEachTargetPermanentEffect()),
                "{X}{X}{R}, {T}, Sacrifice this creature: Destroy X target lands.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Targets must be lands"
                ),
                null, null, null, List.of(), 0, 100)
                .withXScaledTargets());
    }
}
