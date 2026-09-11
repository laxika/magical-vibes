package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "61")
public class GuildThief extends Card {

    public GuildThief() {
        // Whenever this creature deals combat damage to a player, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // {3}{U}: This creature can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{3}{U}: This creature can't be blocked this turn."
        ));
    }
}
