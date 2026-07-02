package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerPoisoned;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "MBS", collectorNumber = "55")
public class SepticRats extends Card {

    public SepticRats() {
        // Whenever Septic Rats attacks, if defending player is poisoned,
        // Septic Rats gets +1/+1 until end of turn.
        // (Infect is auto-loaded from Scryfall.)
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(new DefendingPlayerPoisoned(), new BoostSelfEffect(1, 1)));
    }
}
