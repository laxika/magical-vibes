package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "261")
public class RevelsongHorn extends Card {

    public RevelsongHorn() {
        // {1}, {T}, Tap an untapped creature you control: Target creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate()),
                        new BoostTargetCreatureEffect(1, 1)),
                "{1}, {T}, Tap an untapped creature you control: Target creature gets +1/+1 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
