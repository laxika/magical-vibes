package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "MID", collectorNumber = "163")
@CardRegistration(set = "DBL", collectorNumber = "163")
public class TavernRuffian extends Card {

    public TavernRuffian() {
        setBackFaceCard(new TavernSmasher());
    }

    @Override
    public String getBackFaceClassName() {
        return "TavernSmasher";
    }
}
