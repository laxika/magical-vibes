package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "THB", collectorNumber = "162")
public class UnderworldFires extends Card {

    public UnderworldFires() {
        addSpellEffect(DealDamageEffect.toEachCreatureAndPlaneswalker(1).exilingPermanentsDealtDamage());
    }
}
