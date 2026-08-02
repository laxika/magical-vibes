package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M15", collectorNumber = "198")
public class SatyrWayfinder extends Card {

    public SatyrWayfinder() {
        // When this creature enters, reveal the top four cards of your library. You may put a land
        // card from among them into your hand. Put the rest into your graveyard.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayRevealOneToHandRestToGraveyard(4,
                        new CardTypePredicate(CardType.LAND)));
    }
}
