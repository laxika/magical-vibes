package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;

@CardRegistration(set = "SOI", collectorNumber = "102")
@CardRegistration(set = "SIR", collectorNumber = "99")
public class BitingRain extends Card {

    public BitingRain() {
        addSpellEffect(BoostEffect.allCreatures(-2, -2));
        addCastingOption(new MadnessCast("{2}{B}"));
    }
}
