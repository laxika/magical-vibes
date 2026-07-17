package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "310")
@CardRegistration(set = "5ED", collectorNumber = "399")
public class SkullCatapult extends Card {

    public SkullCatapult() {
        // {1}, {T}, Sacrifice a creature: This artifact deals 2 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{1}, {T}, Sacrifice a creature: Skull Catapult deals 2 damage to any target."
        ));
    }
}
