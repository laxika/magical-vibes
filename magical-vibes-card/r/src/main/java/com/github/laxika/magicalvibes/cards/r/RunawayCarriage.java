package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;

@CardRegistration(set = "SOI", collectorNumber = "261")
public class RunawayCarriage extends Card {

    public RunawayCarriage() {
        addEffect(EffectSlot.ON_ATTACK, new SacrificeAtEndOfCombatEffect());
        addEffect(EffectSlot.ON_BLOCK, new SacrificeAtEndOfCombatEffect());
    }
}
