package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "80")
public class TruePolymorph extends Card {

    public TruePolymorph() {
        target(new PermanentPredicateTargetFilter(
                artifactOrCreature(), "Target must be an artifact or creature"));
        target(new PermanentPredicateTargetFilter(
                artifactOrCreature(), "Second target must be an artifact or creature"))
                .addEffect(EffectSlot.SPELL, new MakeTargetCopyOfTargetPermanentEffect());
    }

    private static PermanentAnyOfPredicate artifactOrCreature() {
        return new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
    }
}
