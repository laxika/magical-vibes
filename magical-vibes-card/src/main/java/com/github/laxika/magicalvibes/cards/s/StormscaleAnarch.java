package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardDealDamageEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "74")
public class StormscaleAnarch extends Card {

    public StormscaleAnarch() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new DiscardRandomCardCost(), new DiscardRandomCardDealDamageEffect(2, 4)),
                "{2}{R}, Discard a card at random: This creature deals 2 damage to any target. If the discarded card was multicolored, this creature deals 4 damage instead."
        ));
    }
}
