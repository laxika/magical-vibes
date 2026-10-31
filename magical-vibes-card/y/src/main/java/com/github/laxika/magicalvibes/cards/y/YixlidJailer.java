package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardsLoseAllAbilitiesEffect;

@CardRegistration(set = "FUT", collectorNumber = "93")
@CardRegistration(set = "TSR", collectorNumber = "149")
public class YixlidJailer extends Card {

    public YixlidJailer() {
        addStaticEffect(new GraveyardCardsLoseAllAbilitiesEffect());
    }
}
