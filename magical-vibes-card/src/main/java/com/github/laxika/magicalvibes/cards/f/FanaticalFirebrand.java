package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "195")
public class FanaticalFirebrand extends Card {

    public FanaticalFirebrand() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(1)),
                "{T}, Sacrifice Fanatical Firebrand: It deals 1 damage to any target."
        ));
    }
}
