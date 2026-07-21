package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "ARB", collectorNumber = "42")
public class LightningReaver extends Card {

    public LightningReaver() {
        // Haste and fear are keywords auto-loaded from Scryfall.

        // Whenever this creature deals combat damage to a player, put a charge counter on it.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutCountersOnSelfEffect(CounterType.CHARGE));

        // At the beginning of your end step, this creature deals damage equal to the number of
        // charge counters on it to each opponent.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new DealDamageToPlayersEffect(new CountersOnSource(CounterType.CHARGE), DamageRecipient.EACH_OPPONENT));
    }
}
