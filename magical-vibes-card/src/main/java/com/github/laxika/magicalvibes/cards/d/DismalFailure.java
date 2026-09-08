package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDiscardsEffect;

@CardRegistration(set = "PLC", collectorNumber = "39")
public class DismalFailure extends Card {

    public DismalFailure() {
        // Discard before countering so the target spell is still on the stack when its controller
        // is resolved. The discard still happens if the spell cannot be countered.
        addEffect(EffectSlot.SPELL, new TargetSpellControllerDiscardsEffect(1));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
