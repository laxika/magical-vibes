package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "163")
public class SoulChanneling extends Card {

    public SoulChanneling() {
        target(TargetFilters.creature());
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(2), new RegenerateEffect()),
                "Pay 2 life: Regenerate enchanted creature."
        ));
    }
}
