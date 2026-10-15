package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "KTK", collectorNumber = "228")
public class WitnessOfTheAges extends Card {

    public WitnessOfTheAges() {
        addMorph("{5}");
    }
}
