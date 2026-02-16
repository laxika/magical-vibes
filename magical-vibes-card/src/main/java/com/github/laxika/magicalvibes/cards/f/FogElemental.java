package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;

public class FogElemental extends Card {

    public FogElemental() {
        addEffect(EffectSlot.ON_ATTACK, new SacrificeAtEndOfCombatEffect());
        addEffect(EffectSlot.ON_BLOCK, new SacrificeAtEndOfCombatEffect());
    }
}
