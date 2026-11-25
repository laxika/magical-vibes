package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "RTR", collectorNumber = "94")
public class ExplosiveImpact extends Card {

    public ExplosiveImpact() {
        // Explosive Impact deals 5 damage to any target.
        addSpellEffect(DealDamageEffect.toAnyTarget(5));
    }
}
