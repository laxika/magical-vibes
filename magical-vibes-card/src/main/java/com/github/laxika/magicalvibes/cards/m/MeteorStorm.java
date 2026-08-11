package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "256")
public class MeteorStorm extends Card {

    public MeteorStorm() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{G}",
                List.of(
                        new DiscardRandomCardCost(2),
                        new DealDamageToAnyTargetEffect(4)
                ),
                "{2}{R}{G}, Discard two cards at random: This enchantment deals 4 damage to any target."
        ));
    }
}
