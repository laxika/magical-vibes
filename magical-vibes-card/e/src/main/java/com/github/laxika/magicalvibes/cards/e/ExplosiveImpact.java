package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "RTR", collectorNumber = "94")
public class ExplosiveImpact extends Card {

    public ExplosiveImpact() {
        // Explosive Impact deals 5 damage to any target.
        addSpellEffect(new DealDamageToAnyTargetEffect(5));
    }
}
