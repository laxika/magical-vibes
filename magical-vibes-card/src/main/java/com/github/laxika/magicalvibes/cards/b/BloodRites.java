package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "159")
public class BloodRites extends Card {

    public BloodRites() {
        // {1}{R}, Sacrifice a creature: This enchantment deals 2 damage to any target. Both the mana
        // and the sacrifice are written before the colon, so they are costs paid at activation.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new SacrificeCreatureCost(),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{1}{R}, Sacrifice a creature: This enchantment deals 2 damage to any target."
        ));
    }
}
