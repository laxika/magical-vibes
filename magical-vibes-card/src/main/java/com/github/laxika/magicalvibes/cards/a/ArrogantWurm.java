package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;

@CardRegistration(set = "TOR", collectorNumber = "120")
public class ArrogantWurm extends Card {

    public ArrogantWurm() {
        addCastingOption(new MadnessCast("{2}{G}"));
    }
}
