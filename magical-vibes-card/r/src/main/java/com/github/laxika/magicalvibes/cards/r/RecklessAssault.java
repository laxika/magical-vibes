package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "263")
public class RecklessAssault extends Card {

    public RecklessAssault() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new PayLifeCost(2), new DealDamageToAnyTargetEffect(1)),
                "{1}, Pay 2 life: This enchantment deals 1 damage to any target."
        ));
    }
}
