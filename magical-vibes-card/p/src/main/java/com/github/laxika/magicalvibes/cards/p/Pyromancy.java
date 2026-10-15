package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "88")
public class Pyromancy extends Card {

    public Pyromancy() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new DiscardRandomCardCost(),
                        new DealDamageToAnyTargetEffect(new LastDiscardedCardManaValue())
                ),
                "{3}, Discard a card at random: This enchantment deals damage to any target equal to the mana value of the discarded card."
        ));
    }
}
