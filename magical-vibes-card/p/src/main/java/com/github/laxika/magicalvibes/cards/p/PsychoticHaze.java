package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "TOR", collectorNumber = "76")
public class PsychoticHaze extends Card {

    public PsychoticHaze() {
        addSpellEffect(new MassDamageEffect(1, true));
        addCastingOption(new MadnessCast("{1}{B}"));
    }
}
