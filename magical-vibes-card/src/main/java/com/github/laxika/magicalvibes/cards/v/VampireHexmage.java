package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "114")
public class VampireHexmage extends Card {

    public VampireHexmage() {
        // Sacrifice this creature: Remove all counters from target permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new RemoveAllCountersFromTargetPermanentEffect()),
                "Sacrifice this creature: Remove all counters from target permanent.",
                TargetFilters.permanent()
        ));
    }
}
