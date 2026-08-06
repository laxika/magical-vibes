package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "GTC", collectorNumber = "189")
public class PsychicStrike extends Card {

    public PsychicStrike() {
        // Counter target spell. Its controller mills two cards. The mill is listed before the
        // counter so the spell is still on the stack when TARGET_SPELL_CONTROLLER is resolved
        // (rules-equivalent; also mills for an uncounterable spell).
        addEffect(EffectSlot.SPELL, new MillEffect(2, MillRecipient.TARGET_SPELL_CONTROLLER));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
