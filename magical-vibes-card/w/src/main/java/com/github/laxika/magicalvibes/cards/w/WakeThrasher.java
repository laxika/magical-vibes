package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "31")
@CardRegistration(set = "DDT", collectorNumber = "24")
public class WakeThrasher extends Card {

    public WakeThrasher() {
        // Whenever a permanent you control becomes untapped, this creature gets +1/+1 until end of turn.
        // Fires once per untapped permanent (including itself untapping during the untap step).
        addTrigger(Triggers.permanentYouControlBecomesUntapped(), BoostEffect.self(1, 1));
    }
}
