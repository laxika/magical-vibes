package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "KHM", collectorNumber = "129")
public class DemonBolt extends Card {

    public DemonBolt() {
        addSpellEffect(DealDamageEffect.toTargetCreatureOrPlaneswalker(4));
        addCastingOption(new ForetellCast("{R}"));
    }
}
