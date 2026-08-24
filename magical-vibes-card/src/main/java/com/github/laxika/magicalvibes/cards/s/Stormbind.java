package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "304")
@CardRegistration(set = "TSB", collectorNumber = "102")
public class Stormbind extends Card {

    public Stormbind() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new DiscardRandomCardCost(),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{2}, Discard a card at random: This enchantment deals 2 damage to any target."
        ));
    }
}
