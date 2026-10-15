package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "160")
public class SilentAssassin extends Card {

    public SilentAssassin() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(new DestroyTargetPermanentAtEndOfCombatEffect()),
                "{3}{B}: Destroy target blocking creature at end of combat.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsBlockingPredicate(),
                        "Target must be a blocking creature."
                )
        ));
    }
}
