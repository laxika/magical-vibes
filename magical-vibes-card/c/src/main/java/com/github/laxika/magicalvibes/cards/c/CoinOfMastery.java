package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ArtifactManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersByAmountEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "38")
public class CoinOfMastery extends Card {

    public CoinOfMastery() {
        addStaticEffect(ControlledPermanentsEnterWithAdditionalCountersByAmountEffect.ofEnteringPermanent(
                new PermanentIsCreaturePredicate(), new ArtifactManaSpentToCast()));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                "{T}: Create a Treasure token."
        ));
    }
}
