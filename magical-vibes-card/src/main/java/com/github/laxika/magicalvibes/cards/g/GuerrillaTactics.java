package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

public class GuerrillaTactics extends Card {

    public GuerrillaTactics() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT, new DealDamageToAnyTargetEffect(4));
    }
}
