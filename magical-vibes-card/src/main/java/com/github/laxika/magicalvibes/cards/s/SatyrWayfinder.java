package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M15", collectorNumber = "198")
@CardRegistration(set = "C15", collectorNumber = "201")
@CardRegistration(set = "BNG", collectorNumber = "136")
@CardRegistration(set = "UMA", collectorNumber = "180")
@CardRegistration(set = "EA2", collectorNumber = "17")
@CardRegistration(set = "TDC", collectorNumber = "267")
@CardRegistration(set = "M3C", collectorNumber = "244")
@CardRegistration(set = "OTC", collectorNumber = "204")
@CardRegistration(set = "C20", collectorNumber = "188")
@CardRegistration(set = "EOC", collectorNumber = "106")
@CardRegistration(set = "C16", collectorNumber = "165")
@CardRegistration(set = "CMA", collectorNumber = "143")
@CardRegistration(set = "ZNC", collectorNumber = "81")
public class SatyrWayfinder extends Card {

    public SatyrWayfinder() {
        // When this creature enters, reveal the top four cards of your library. You may put a land
        // card from among them into your hand. Put the rest into your graveyard.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayRevealOneToHandRestToGraveyard(4,
                        new CardTypePredicate(CardType.LAND)));
    }
}
