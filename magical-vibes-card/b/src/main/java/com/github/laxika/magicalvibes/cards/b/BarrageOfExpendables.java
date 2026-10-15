package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "127")
public class BarrageOfExpendables extends Card {

    public BarrageOfExpendables() {
        // {R}, Sacrifice a creature: This enchantment deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new SacrificeCreatureCost(),
                        new DealDamageToAnyTargetEffect(1)
                ),
                "{R}, Sacrifice a creature: This enchantment deals 1 damage to any target."
        ));
    }
}
