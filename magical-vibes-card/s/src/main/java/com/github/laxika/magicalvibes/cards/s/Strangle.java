package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "SNC", collectorNumber = "125")
public class Strangle extends Card {

    public Strangle() {
        addSpellEffect(new DealDamageToTargetCreatureOrPlaneswalkerEffect(3));
    }
}
