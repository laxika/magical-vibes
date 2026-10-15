package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

public class NeoExdeathDimensionsEnd extends Card {

    public NeoExdeathDimensionsEnd() {
        CardsInGraveyard permanentCards =
                new CardsInGraveyard(new CardIsPermanentPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(permanentCards, new Fixed(3)));
    }
}
