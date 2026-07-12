package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "8ED", collectorNumber = "171")
public class ViciousHunger extends Card {

    public ViciousHunger() {
        // Vicious Hunger deals 2 damage to target creature and you gain 2 life.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(2));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
