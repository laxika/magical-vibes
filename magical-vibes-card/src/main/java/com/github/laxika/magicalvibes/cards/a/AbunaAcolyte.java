package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "1")
public class AbunaAcolyte extends Card {

    public AbunaAcolyte() {
        // {T}: Prevent the next 1 damage that would be dealt to any target this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PreventDamageToTargetEffect(1)),
                "{T}: Prevent the next 1 damage that would be dealt to any target this turn."
        ));

        // {T}: Prevent the next 2 damage that would be dealt to target artifact creature this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PreventDamageToTargetEffect(2)),
                "{T}: Prevent the next 2 damage that would be dealt to target artifact creature this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        "Target must be an artifact creature"
                )
        ));
    }
}
