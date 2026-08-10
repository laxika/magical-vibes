package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "91")
public class OgreShaman extends Card {

    public OgreShaman() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new DiscardRandomCardCost(),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{2}, Discard a card at random: This creature deals 2 damage to any target."
        ));
    }
}
