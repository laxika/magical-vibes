package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;

@CardRegistration(set = "P02", collectorNumber = "131")
@CardRegistration(set = "PTK", collectorNumber = "140")
@CardRegistration(set = "8ED", collectorNumber = "262")
public class LoneWolf extends Card {

    public LoneWolf() {
        addEffect(EffectSlot.STATIC, new AssignCombatDamageAsThoughUnblockedEffect());
    }
}
