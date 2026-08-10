package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "86")
public class MageIlVec extends Card {

    public MageIlVec() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DiscardRandomCardCost(), new DealDamageToAnyTargetEffect(1)),
                "{T}, Discard a card at random: This creature deals 1 damage to any target."
        ));
    }
}
