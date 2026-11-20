package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;

@CardRegistration(set = "PLS", collectorNumber = "10")
public class MarchOfSouls extends Card {

    public MarchOfSouls() {
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate(), true));
        addSpellEffect(new EachPlayerCreatesTokenEffect(CreateTokenEffect.whiteSpirit(1)
                .withAmount(new AmountThisWay(StepKind.DESTROY).controlledByYou())));
    }
}
