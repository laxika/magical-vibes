package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "113")
public class FlowstoneEmbrace extends Card {

    public FlowstoneEmbrace() {
        target(TargetFilters.creature());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(BoostEffect.of(Subjects.attached(), new Fixed(2), new Fixed(-2))),
                "{T}: Enchanted creature gets +2/-2 until end of turn."
        ));
    }
}
