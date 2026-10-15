package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "322")
public class UnerringSling extends Card {

    public UnerringSling() {
        // {3}, {T}, Tap an untapped creature you control: This artifact deals damage equal to the
        // tapped creature's power to target attacking or blocking creature with flying.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate(), false, true),
                        new DealDamageToTargetCreatureEffect(new ChosenPermanentPower())),
                "{3}, {T}, Tap an untapped creature you control: This artifact deals damage equal to "
                        + "the tapped creature's power to target attacking or blocking creature with flying.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsAttackingPredicate(),
                                        new PermanentIsBlockingPredicate()
                                )),
                                new PermanentHasKeywordPredicate(Keyword.FLYING)
                        )),
                        "Target must be an attacking or blocking creature with flying"
                )));
    }
}
