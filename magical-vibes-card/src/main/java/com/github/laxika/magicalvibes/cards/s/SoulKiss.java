package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "162")
public class SoulKiss extends Card {

    public SoulKiss() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));

        // {B}, Pay 1 life: Enchanted creature gets +2/+2 until end of turn.
        // Activate no more than three times each turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new PayLifeCost(1),
                        new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(2), new Fixed(2))
                ),
                "{B}, Pay 1 life: Enchanted creature gets +2/+2 until end of turn. Activate no more than three times each turn.",
                3
        ));
    }
}
