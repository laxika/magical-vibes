package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "9")
public class Blessing extends Card {

    public Blessing() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(1), new Fixed(1))),
                "{W}: Enchanted creature gets +1/+1 until end of turn."
        ));
    }
}
