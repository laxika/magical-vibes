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
        // Counter target spell. Its controller mills two cards. The counter is applied first (CR 608.2c),
        // so the countered spell enters the graveyard before the milled cards; the mill still finds the
        // spell's controller through the countered-spell record kept on the stack entry.
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new MillEffect(2, MillRecipient.TARGET_SPELL_CONTROLLER));
    }
}
