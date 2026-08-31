package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandSharingChosenCostPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "306")
public class CrypticGateway extends Card {

    public CrypticGateway() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate(), false, true),
                        new MayEffect(
                                new PutCreatureFromHandSharingChosenCostPermanentsEffect(),
                                "Put a creature card from your hand that shares a creature type with each creature tapped this way onto the battlefield?"
                        )
                ),
                "Tap two untapped creatures you control: You may put a creature card from your hand that shares a creature type with each creature tapped this way onto the battlefield."
        ));
    }
}
