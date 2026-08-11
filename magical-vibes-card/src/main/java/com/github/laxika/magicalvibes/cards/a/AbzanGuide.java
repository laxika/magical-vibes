package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "KTK", collectorNumber = "162")
public class AbzanGuide extends Card {

    public AbzanGuide() {
        addMorph("{2}{W}{B}{G}");
    }
}
