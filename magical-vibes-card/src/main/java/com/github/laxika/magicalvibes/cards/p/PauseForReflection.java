package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "GRN", collectorNumber = "140")
public class PauseForReflection extends Card {

    public PauseForReflection() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombat());
    }
}
