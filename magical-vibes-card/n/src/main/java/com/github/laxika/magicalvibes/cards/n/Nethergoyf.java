package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

import java.util.List;

@CardRegistration(set = "MH3", collectorNumber = "103")
public class Nethergoyf extends Card {

    public Nethergoyf() {
        CardTypesAmongCardsInGraveyard cardTypes =
                new CardTypesAmongCardsInGraveyard(CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                cardTypes, new Sum(cardTypes, new Fixed(1))));

        addCastingOption(new GraveyardCast(null, "{2}{B}", List.of(
                ExileNCardsFromGraveyardCastingCost.anyNumberWithCardTypes(4)),
                null, false, false, true));
    }
}
