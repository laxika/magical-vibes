package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "GTC", collectorNumber = "84")
public class WightOfPrecinctSix extends Card {

    public WightOfPrecinctSix() {
        // Wight of Precinct Six gets +1/+1 for each creature card in your opponents' graveyards.
        CardsInGraveyard creatureCardsInOpponentGraveyards =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.OPPONENTS);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                creatureCardsInOpponentGraveyards, creatureCardsInOpponentGraveyards));
    }
}
