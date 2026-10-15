package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ODY", collectorNumber = "13")
public class Cantivore extends Card {

    public Cantivore() {
        CardsInGraveyard enchantmentsInAllGraveyards =
                new CardsInGraveyard(new CardTypePredicate(CardType.ENCHANTMENT), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(enchantmentsInAllGraveyards, enchantmentsInAllGraveyards));
    }
}
