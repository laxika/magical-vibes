package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;

@CardRegistration(set = "MH2", collectorNumber = "89")
public class KitchenImp extends Card {

    public KitchenImp() {
        addCastingOption(new MadnessCast("{B}"));
    }
}
