package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAmongDefendingPlayerAndCreaturesEffect;

@CardRegistration(set = "ONS", collectorNumber = "192")
public class ButcherOrgg extends Card {

    public ButcherOrgg() {
        addEffect(EffectSlot.STATIC, new AssignCombatDamageAmongDefendingPlayerAndCreaturesEffect());
    }
}
