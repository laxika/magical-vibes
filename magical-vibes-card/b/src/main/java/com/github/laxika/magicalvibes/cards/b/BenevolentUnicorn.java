package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReduceSpellDamageEffect;

@CardRegistration(set = "MIR", collectorNumber = "4")
public class BenevolentUnicorn extends Card {

    public BenevolentUnicorn() {
        // If a spell would deal damage to a permanent or player, it deals that much damage minus 1
        // to that permanent or player instead.
        addStaticEffect(new ReduceSpellDamageEffect(1));
    }
}
