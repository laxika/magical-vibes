package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "127")
public class DeepfireElemental extends Card {

    public DeepfireElemental() {
        PermanentPredicate artifactOrCreatureWithManaValueX = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                new PermanentManaValueEqualsXPredicate()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{X}{1}",
                List.of(new DestroyTargetPermanentEffect()),
                "{X}{X}{1}: Destroy target artifact or creature with mana value X.",
                new PermanentPredicateTargetFilter(
                        artifactOrCreatureWithManaValueX,
                        "Target must be an artifact or creature with mana value X"
                )
        ));
    }
}
