package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "161")
public class WineOfBloodAndIron extends Card {

    public WineOfBloodAndIron() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new BoostTargetCreatureEffect(new TargetPower(), new Fixed(0)),
                        new SacrificeSelfAtEndStepEffect()),
                "{4}: Target creature gets +X/+0 until end of turn, where X is its power. Sacrifice this artifact at the beginning of the next end step.",
                TargetFilters.creature()));
    }
}
