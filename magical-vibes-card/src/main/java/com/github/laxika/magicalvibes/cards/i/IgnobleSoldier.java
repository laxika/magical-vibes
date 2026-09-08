package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "MMQ", collectorNumber = "22")
public class IgnobleSoldier extends Card {

    public IgnobleSoldier() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, PreventDamageEffect.allCombatBySelf());
    }
}
