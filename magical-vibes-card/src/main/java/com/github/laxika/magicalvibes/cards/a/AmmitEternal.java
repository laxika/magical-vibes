package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromSelfEffect;

@CardRegistration(set = "HOU", collectorNumber = "57")
public class AmmitEternal extends Card {

    public AmmitEternal() {
        // Afflict 3 — whenever this creature becomes blocked, the defending player loses 3 life
        // (once per becoming blocked, not per blocker). Heads-up, so the sole opponent is the defender.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT));
        // Whenever an opponent casts a spell, put a -1/-1 counter on this creature (mandatory —
        // the source is bound by the dedicated ON_OPPONENT_CASTS_SPELL collector).
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new PutCountersOnSourceEffect(-1, -1, 1));
        // Whenever this creature deals combat damage to a player, remove all -1/-1 counters from it.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RemoveAllCountersFromSelfEffect(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
