package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "174")
public class RoyalAssassin extends Card {

    public RoyalAssassin() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetCreatureEffect(false)),
                true,
                "{T}: Destroy target tapped creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsTappedPredicate(),
                        "Target must be a tapped creature"
                )
        ));
    }
}
