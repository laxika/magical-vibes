package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "58")
public class DauthiJackal extends Card {

    public DauthiJackal() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect(false)),
                "{B}{B}, Sacrifice Dauthi Jackal: Destroy target blocking creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsBlockingPredicate(),
                        "Target must be a blocking creature."
                )
        ));
    }
}
