package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "BFZ", collectorNumber = "151")
@CardRegistration(set = "PIO", collectorNumber = "377")
@CardRegistration(set = "DMC", collectorNumber = "126")
public class RadiantFlames extends Card {

    public RadiantFlames() {
        addSpellEffect(DealDamageEffect.toEachCreature(new XValue()));
    }
}
