package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "32")
public class AvenAugur extends Card {

    public AvenAugur() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), ReturnToHandEffect.target()),
                "Sacrifice this creature: Return up to two target creatures to their owners' hands. Activate only during your upkeep.",
                null,
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP,
                List.of(TargetFilters.creature(), TargetFilters.creature()),
                0,
                2
        ));
    }
}
