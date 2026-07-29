package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PreventPhaseOutTargetPermanentEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "284")
public class SpatialBinding extends Card {

    public SpatialBinding() {
        // Pay 1 life: Until your next upkeep, target permanent can't phase out.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(1), new PreventPhaseOutTargetPermanentEffect()),
                "Pay 1 life: Until your next upkeep, target permanent can't phase out."));
    }
}
