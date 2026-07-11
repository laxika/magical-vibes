package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "POR", collectorNumber = "120")
public class BurningCloak extends Card {

    public BurningCloak() {
        // Target creature gets +2/+0 until end of turn, then take 2 damage. Both effects share the
        // single auto-derived creature target.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 0));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(2));
    }
}
