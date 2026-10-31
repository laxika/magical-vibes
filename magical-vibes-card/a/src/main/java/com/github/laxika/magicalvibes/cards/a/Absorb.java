package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "INV", collectorNumber = "226")
@CardRegistration(set = "RNA", collectorNumber = "151")
@CardRegistration(set = "DMR", collectorNumber = "186")
@CardRegistration(set = "SCD", collectorNumber = "216")
public class Absorb extends Card {

    public Absorb() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new GainLifeEffect(3));
    }
}
