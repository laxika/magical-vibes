package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "APC", collectorNumber = "8")
public class DivineLight extends Card {

    public DivineLight() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allToControlledCreatures());
    }
}
