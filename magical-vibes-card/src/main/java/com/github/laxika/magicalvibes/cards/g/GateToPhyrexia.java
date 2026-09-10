package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ATQ", collectorNumber = "16")
public class GateToPhyrexia extends Card {

    public GateToPhyrexia() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeCreatureCost(), new DestroyTargetPermanentEffect()),
                "Sacrifice a creature: Destroy target artifact. Activate only during your upkeep and only once each turn.",
                TargetFilters.artifact(),
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
