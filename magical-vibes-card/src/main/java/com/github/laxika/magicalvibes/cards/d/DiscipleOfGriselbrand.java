package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToXValueEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "98")
public class DiscipleOfGriselbrand extends Card {

    public DiscipleOfGriselbrand() {
        // {1}, Sacrifice a creature: You gain life equal to the sacrificed creature's toughness.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeCreatureCost(false, false, true), new GainLifeEqualToXValueEffect()),
                "{1}, Sacrifice a creature: You gain life equal to the sacrificed creature's toughness.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )
        ));
    }
}
