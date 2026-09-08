package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "GRN", collectorNumber = "179")
public class Ionize extends Card {

    public Ionize() {
        // Deal damage before countering so the targeted spell is still on the stack.
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_SPELL_CONTROLLER));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
