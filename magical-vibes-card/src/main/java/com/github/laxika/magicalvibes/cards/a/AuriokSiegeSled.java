package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "105")
public class AuriokSiegeSled extends Card {

    public AuriokSiegeSled() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new MustBlockSourceEffect(null)),
                "{1}: Target artifact creature blocks Auriok Siege Sled this turn if able.",
                artifactCreatureTarget()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new CantBlockSourceEffect(null)),
                "{1}: Target artifact creature can't block Auriok Siege Sled this turn.",
                artifactCreatureTarget()
        ));
    }

    private static PermanentPredicateTargetFilter artifactCreatureTarget() {
        return new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                "Target must be an artifact creature"
        );
    }
}
