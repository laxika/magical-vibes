package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "SHM", collectorNumber = "86")
public class BurnTrail extends Card {

    public BurnTrail() {
        // Burn Trail deals 3 damage to any target.
        // (Conspire is driven by the Scryfall-loaded CONSPIRE keyword and handled by the casting flow.)
        addSpellEffect(new DealDamageToAnyTargetEffect(3));
    }
}
