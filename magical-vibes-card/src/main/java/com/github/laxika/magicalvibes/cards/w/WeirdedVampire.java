package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;

@CardRegistration(set = "EMN", collectorNumber = "113")
public class WeirdedVampire extends Card {

    public WeirdedVampire() {
        // Madness {2}{B}
        addCastingOption(new MadnessCast("{2}{B}"));
    }
}
