package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "JOU", collectorNumber = "33")
public class Countermand extends Card {

    public Countermand() {
        // Mill before countering so TARGET_SPELL_CONTROLLER can still resolve against the spell
        // on the stack, including when the spell can't be countered.
        addEffect(EffectSlot.SPELL, new MillEffect(4, MillRecipient.TARGET_SPELL_CONTROLLER));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
