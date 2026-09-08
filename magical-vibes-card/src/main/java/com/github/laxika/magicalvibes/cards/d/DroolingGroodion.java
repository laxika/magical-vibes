package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "204")
public class DroolingGroodion extends Card {

    public DroolingGroodion() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{G}",
                List.of(
                        new SacrificeCreatureCost(),
                        BoostTargetCreatureEffect.forTargetGroup(2, 2, 0),
                        BoostTargetCreatureEffect.forTargetGroup(-2, -2, 1)
                ),
                "{2}{B}{G}, Sacrifice a creature: Target creature gets +2/+2 until end of turn. "
                        + "Another target creature gets -2/-2 until end of turn.",
                List.of(TargetFilters.creature(), TargetFilters.creature()),
                2,
                2
        ));
    }
}
