package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "29")
@CardRegistration(set = "MKC", collectorNumber = "81")
@CardRegistration(set = "FDC", collectorNumber = "44")
public class SearchThePremises extends Card {

    public SearchThePremises() {
        // Whenever a creature attacks you or a planeswalker you control, investigate.
        addTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker(), CreateTokenEffect.ofClueToken(1));
    }
}
