package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "42")
public class BlindCreeper extends Card {

    public BlindCreeper() {
        // Whenever a player casts a spell, this creature gets -1/-1 until end of turn.
        addTrigger(Triggers.playerCastsSpell().anySpell(), BoostEffect.self(-1, -1));
    }
}
