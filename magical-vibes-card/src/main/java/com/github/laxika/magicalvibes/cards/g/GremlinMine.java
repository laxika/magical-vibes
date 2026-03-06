package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromTargetPermanentEffect;
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
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(4)),
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
                List.of(new SacrificeSelfCost(), new RemoveChargeCountersFromTargetPermanentEffect(4)),
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
