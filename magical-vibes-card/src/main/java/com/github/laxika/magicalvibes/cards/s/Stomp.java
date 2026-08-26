package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

public class Stomp extends Card {

    public Stomp() {
        addEffect(EffectSlot.SPELL, new DamageCantBePreventedThisTurnEffect());
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
    }
}
