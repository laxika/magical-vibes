package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "241")
public class SpringjawTrap extends Card {

    public SpringjawTrap() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(3)),
                "{4}, {T}, Sacrifice this artifact: It deals 3 damage to any target."
        ));
    }
}
