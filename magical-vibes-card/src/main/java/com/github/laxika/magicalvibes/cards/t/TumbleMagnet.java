package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "218")
public class TumbleMagnet extends Card {

    public TumbleMagnet() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(3));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new TapTargetPermanentEffect()
                ),
                "{T}, Remove a charge counter from Tumble Magnet: Tap target artifact or creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        "Target must be an artifact or creature"
                )
        ));
    }
}
