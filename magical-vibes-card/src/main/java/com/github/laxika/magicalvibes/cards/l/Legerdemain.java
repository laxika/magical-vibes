package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "72")
public class Legerdemain extends Card {

    public Legerdemain() {
        // "Exchange control of target artifact or creature and another target permanent that shares
        // one of those types with it." Because the second target must share the artifact or creature
        // type, it is itself an artifact or a creature, so both positions use the same filter and the
        // cross-target restriction is enforced at announcement by the targeting services. Either
        // target may be controlled by any player; if both are under the same controller the exchange
        // does nothing (CR 701.12b), and if either target is illegal at resolution nothing happens
        // at all (CR 701.12a).
        setMultiTargetConstraint(MultiTargetConstraint.SHARE_ARTIFACT_OR_CREATURE_TYPE);

        target(new PermanentPredicateTargetFilter(
                artifactOrCreature(),
                "First target must be an artifact or creature"));

        target(new PermanentPredicateTargetFilter(
                artifactOrCreature(),
                "Second target must be an artifact or creature sharing a type with the first target"))
                .addEffect(EffectSlot.SPELL, new ExchangeControlOfTargetPermanentsEffect(
                        artifactOrCreature(), false, false, false, true));
    }

    private static PermanentAnyOfPredicate artifactOrCreature() {
        return new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
    }
}
