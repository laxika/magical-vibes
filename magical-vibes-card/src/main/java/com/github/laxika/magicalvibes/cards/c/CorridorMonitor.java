package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "41")
public class CorridorMonitor extends Card {

    public CorridorMonitor() {
        ControlledPermanentPredicateTargetFilter artifactOrCreatureYouControl =
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        "Target must be an artifact or creature you control");

        target(artifactOrCreatureYouControl)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new UntapPermanentsEffect(TapUntapScope.TARGET, artifactOrCreatureYouControl.predicate()));
    }
}
