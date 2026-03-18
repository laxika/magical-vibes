package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "247")
public class StensiaBloodhall extends Card {

    public StensiaBloodhall() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {3}{B}{R}, {T}: Stensia Bloodhall deals 2 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{B}{R}",
                List.of(new DealDamageToAnyTargetEffect(2)),
                "{3}{B}{R}, {T}: Stensia Bloodhall deals 2 damage to target player or planeswalker.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        "Target must be a player or planeswalker"
                )
        ));
    }
}
