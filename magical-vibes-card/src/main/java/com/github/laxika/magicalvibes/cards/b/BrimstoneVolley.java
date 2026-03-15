package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MorbidReplacementEffect;

@CardRegistration(set = "ISD", collectorNumber = "132")
public class BrimstoneVolley extends Card {

    public BrimstoneVolley() {
        // Brimstone Volley deals 3 damage to any target.
        // Morbid — Brimstone Volley deals 5 damage instead if a creature died this turn.
        addEffect(EffectSlot.SPELL, new MorbidReplacementEffect(
                new DealDamageToAnyTargetEffect(3),
                new DealDamageToAnyTargetEffect(5)
        ));
    }
}
