package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;

@CardRegistration(set = "KTK", collectorNumber = "115")
public class MarduBlazebringer extends Card {

    public MarduBlazebringer() {
        addEffect(EffectSlot.ON_ATTACK, new SacrificeAtEndOfCombatEffect());
        addEffect(EffectSlot.ON_BLOCK, new SacrificeAtEndOfCombatEffect());
    }
}
