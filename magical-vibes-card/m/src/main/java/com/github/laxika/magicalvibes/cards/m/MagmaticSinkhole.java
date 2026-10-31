package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DelveCost;

@CardRegistration(set = "MH1", collectorNumber = "135")
public class MagmaticSinkhole extends Card {

    public MagmaticSinkhole() {
        addCastingCost(new DelveCost());
        addSpellEffect(new DealDamageToTargetCreatureOrPlaneswalkerEffect(5));
    }
}
