package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "MIR", collectorNumber = "250")
public class UnyaroBeeSting extends Card {

    public UnyaroBeeSting() {
        // "Unyaro Bee Sting deals 2 damage to any target."
        addSpellEffect(DealDamageEffect.toAnyTarget(2));
    }
}
