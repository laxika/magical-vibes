package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "69")
public class TradeTheHelm extends Card {

    public TradeTheHelm() {
        var artifactOrCreature = artifactOrCreature();
        target(new ControlledPermanentPredicateTargetFilter(
                artifactOrCreature,
                "First target must be an artifact or creature you control"));
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        artifactOrCreature,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                "Second target must be an artifact or creature an opponent controls"))
                .addEffect(EffectSlot.SPELL, new ExchangeControlOfTargetPermanentsEffect(
                        artifactOrCreature, false));
        addCycling("{2}");
    }

    private static PermanentAnyOfPredicate artifactOrCreature() {
        return new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
    }
}
