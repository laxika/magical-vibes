package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "136")
public class GremlinMine extends Card {

    public GremlinMine() {
        // {1}, {T}, Sacrifice Gremlin Mine: It deals 4 damage to target artifact creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), DealDamageEffect.toTargetCreature(4)),
                "{1}, {T}, Sacrifice Gremlin Mine: It deals 4 damage to target artifact creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        "Target must be an artifact creature"
                )
        ));

        // {1}, {T}, Sacrifice Gremlin Mine: Remove up to four charge counters from target noncreature artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new RemoveUpToCountersFromAllPermanentsEffect(CounterType.CHARGE, 4, Subjects.target())),
                "{1}, {T}, Sacrifice Gremlin Mine: Remove up to four charge counters from target noncreature artifact.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentNotPredicate(new PermanentIsCreaturePredicate())
                        )),
                        "Target must be a noncreature artifact"
                )
        ));
    }
}
