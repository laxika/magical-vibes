package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "140")
public class WeaponizeTheMonsters extends Card {

    public WeaponizeTheMonsters() {
        // {2}, Sacrifice a creature: This enchantment deals 2 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{2}, Sacrifice a creature: Weaponize the Monsters deals 2 damage to any target."
        ));
    }
}
