package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "STX", collectorNumber = "106")
public class HeatedDebate extends Card {

    public HeatedDebate() {
        addStaticEffect(new CantBeCounteredEffect());
        addSpellEffect(new DealDamageToTargetCreatureOrPlaneswalkerEffect(4));
    }
}
