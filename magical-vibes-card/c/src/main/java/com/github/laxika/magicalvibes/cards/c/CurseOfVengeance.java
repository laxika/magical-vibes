package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C16", collectorNumber = "12")
public class CurseOfVengeance extends Card {

    public CurseOfVengeance() {
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new PutCountersOnSelfEffect(CounterType.SPITE)),
                null,
                null,
                new StackEntryControlledByEnchantedPlayerPredicate(),
                false,
                false));

        CountersOnSource spiteCounters = new CountersOnSource(CounterType.SPITE);
        addTrigger(Triggers.playerLosesGame(), new GainLifeEffect(spiteCounters),
                new DrawCardEffect(spiteCounters));
    }
}
