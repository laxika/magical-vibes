package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "DMU", collectorNumber = "134")
public class JayasFirenado extends Card {

    public JayasFirenado() {
        addSpellEffect(DealDamageEffect.toTargetCreatureOrPlaneswalker(5));
        addSpellEffect(new ScryEffect(1));
    }
}
