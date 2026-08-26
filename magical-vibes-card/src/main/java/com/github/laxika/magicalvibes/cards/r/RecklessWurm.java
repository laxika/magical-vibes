package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;

@CardRegistration(set = "PLC", collectorNumber = "120")
public class RecklessWurm extends Card {

    public RecklessWurm() {
        addCastingOption(new MadnessCast("{2}{R}"));
    }
}
