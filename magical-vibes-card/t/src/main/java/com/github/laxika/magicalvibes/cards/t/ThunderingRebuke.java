package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "ZNR", collectorNumber = "170")
public class ThunderingRebuke extends Card {

    public ThunderingRebuke() {
        addSpellEffect(new DealDamageToTargetCreatureOrPlaneswalkerEffect(4));
    }
}
