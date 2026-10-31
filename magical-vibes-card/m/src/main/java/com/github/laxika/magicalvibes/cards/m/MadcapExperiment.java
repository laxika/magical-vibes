package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MadcapExperimentEffect;

@CardRegistration(set = "KLD", collectorNumber = "122")
public class MadcapExperiment extends Card {

    public MadcapExperiment() {
        addSpellEffect(new MadcapExperimentEffect());
    }
}
