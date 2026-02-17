package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;

import java.util.List;

public class ConeOfFlame extends Card {

    public ConeOfFlame() {
        setNeedsTarget(true);
        setMinTargets(3);
        setMaxTargets(3);
        addEffect(EffectSlot.SPELL, new DealOrderedDamageToAnyTargetsEffect(List.of(1, 2, 3)));
    }
}
