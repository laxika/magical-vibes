package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeNextInstantSorceryUncounterableThisTurnEffect;

@CardRegistration(set = "TOR", collectorNumber = "104")
@CardRegistration(set = "DMR", collectorNumber = "132")
public class Overmaster extends Card {

    public Overmaster() {
        addSpellEffect(new MakeNextInstantSorceryUncounterableThisTurnEffect());
        addSpellEffect(new DrawCardEffect());
    }
}
