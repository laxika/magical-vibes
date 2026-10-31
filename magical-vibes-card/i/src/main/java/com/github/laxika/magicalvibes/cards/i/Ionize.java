package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "GRN", collectorNumber = "179")
@CardRegistration(set = "OTP", collectorNumber = "49")
public class Ionize extends Card {

    public Ionize() {
        // Deal damage before countering so the targeted spell is still on the stack.
        addSpellEffect(new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_SPELL_CONTROLLER));
        addSpellEffect(new CounterSpellEffect());
    }
}
