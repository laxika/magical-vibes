package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "BRO", collectorNumber = "145")
public class ObliteratingBolt extends Card {

    public ObliteratingBolt() {
        addSpellEffect(new DealDamageToTargetCreatureOrPlaneswalkerEffect(4, null, true));
    }
}
