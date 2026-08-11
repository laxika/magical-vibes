package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "34")
public class SynodArtificer extends Card {

    public SynodArtificer() {
        PermanentPredicateTargetFilter noncreatureArtifactFilter = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate())
                )),
                "Targets must be noncreature artifacts"
        );

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{X}, {T}: Tap X target noncreature artifacts.",
                noncreatureArtifactFilter,
                null,
                null,
                null,
                List.of(),
                0,
                100
        ).withXScaledTargets());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS)),
                "{X}, {T}: Untap X target noncreature artifacts.",
                noncreatureArtifactFilter,
                null,
                null,
                null,
                List.of(),
                0,
                100
        ).withXScaledTargets());
    }
}
