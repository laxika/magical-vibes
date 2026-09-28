package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.NonCreatureSubtypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "MB2", collectorNumber = "300")
@CardRegistration(set = "MB2", collectorNumber = "536")
public class Subgoyf extends Card {

    public Subgoyf() {
        NonCreatureSubtypesAmongCardsInGraveyard subtypes =
                new NonCreatureSubtypesAmongCardsInGraveyard(CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                subtypes, new Sum(subtypes, new Fixed(1))));
    }
}
