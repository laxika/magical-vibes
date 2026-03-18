package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "163")
public class SkirsdagCultist extends Card {

    public SkirsdagCultist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new SacrificeCreatureCost(), new DealDamageToAnyTargetEffect(2)),
                "{R}, {T}, Sacrifice a creature: Skirsdag Cultist deals 2 damage to any target."
        ));
    }
}
