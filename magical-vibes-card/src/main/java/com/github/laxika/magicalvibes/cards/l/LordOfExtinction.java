package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ARB", collectorNumber = "91")
public class LordOfExtinction extends Card {

    public LordOfExtinction() {
        // Power and toughness are each equal to the number of cards in all graveyards (any type).
        CardsInGraveyard cardsInAllGraveyards = new CardsInGraveyard(null, CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(cardsInAllGraveyards, cardsInAllGraveyards));
    }
}
