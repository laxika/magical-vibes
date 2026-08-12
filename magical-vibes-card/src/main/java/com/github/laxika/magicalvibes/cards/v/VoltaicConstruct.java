package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "156")
public class VoltaicConstruct extends Card {

    public VoltaicConstruct() {
        PermanentPredicate artifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, artifactCreature)),
                "{2}: Untap target artifact creature.",
                new PermanentPredicateTargetFilter(artifactCreature, "Target must be an artifact creature")
        ));
    }
}
