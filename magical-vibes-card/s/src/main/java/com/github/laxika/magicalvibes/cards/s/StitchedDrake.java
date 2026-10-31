package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

@CardRegistration(set = "ISD", collectorNumber = "80")
@CardRegistration(set = "DDQ", collectorNumber = "49")
@CardRegistration(set = "UMA", collectorNumber = "72")
public class StitchedDrake extends Card {

    public StitchedDrake() {
        addCastingCost(new ExileCardFromGraveyardCost(CardType.CREATURE));
    }
}
