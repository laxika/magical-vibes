package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "231")
public class VonaButcherOfMagan extends Card {

    public VonaButcherOfMagan() {
        // {T}, Pay 7 life: Destroy target nonland permanent. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new PayLifeCost(7),
                        new DestroyTargetPermanentEffect()
                ),
                "{T}, Pay 7 life: Destroy target nonland permanent. Activate only during your turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        "Target must be a nonland permanent"
                ),
                null, null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
