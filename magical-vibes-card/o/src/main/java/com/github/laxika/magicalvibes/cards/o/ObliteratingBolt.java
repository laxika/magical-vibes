package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "BRO", collectorNumber = "145")
public class ObliteratingBolt extends Card {

    public ObliteratingBolt() {
        addSpellEffect(DealDamageEffect.toTargetCreatureOrPlaneswalker(4).exilingIfItWouldDie());
    }
}
