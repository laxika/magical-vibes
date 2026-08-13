package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "USG", collectorNumber = "267")
public class Lull extends Card {

    public Lull() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombat());
        addCycling("{2}");
    }
}
