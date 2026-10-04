package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "115")
@CardRegistration(set = "GK2", collectorNumber = "128")
@CardRegistration(set = "LCC", collectorNumber = "294")
@CardRegistration(set = "C16", collectorNumber = "227")
@CardRegistration(set = "CM2", collectorNumber = "169")
public class VorelOfTheHullClade extends Card {

    public VorelOfTheHullClade() {
        // "{G}{U}, {T}: Double the number of each kind of counter on target artifact,
        // creature, or land."
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{U}",
                List.of(new DoubleCountersOnTargetPermanentEffect()),
                "{G}{U}, {T}: Double the number of each kind of counter on target artifact, creature, or land.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsLandPredicate())),
                        "Target must be an artifact, creature, or land")));
    }
}
