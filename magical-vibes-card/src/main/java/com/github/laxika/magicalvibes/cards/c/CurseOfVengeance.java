package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;

import java.util.List;

@CardRegistration(set = "C16", collectorNumber = "12")
public class CurseOfVengeance extends Card {

    public CurseOfVengeance() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new PutCountersOnSelfEffect(CounterType.SPITE)),
                null,
                null,
                new StackEntryControlledByEnchantedPlayerPredicate(),
                false,
                false));

        CountersOnSource spiteCounters = new CountersOnSource(CounterType.SPITE);
        addEffect(EffectSlot.ON_PLAYER_LOSES_GAME, new GainLifeEffect(spiteCounters));
        addEffect(EffectSlot.ON_PLAYER_LOSES_GAME, new DrawCardEffect(spiteCounters));
    }
}
