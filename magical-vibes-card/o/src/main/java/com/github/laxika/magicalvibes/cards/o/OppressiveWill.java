package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

@CardRegistration(set = "SOK", collectorNumber = "50")
public class OppressiveWill extends Card {

    public OppressiveWill() {
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(new CardsInHand(CountScope.CONTROLLER)));
    }
}
