package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromSelfEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "149")
public class AshlingThePilgrim extends Card {

    public AshlingThePilgrim() {
        // {1}{R}: Put a +1/+1 counter on Ashling the Pilgrim. If this is the third time this ability
        // has resolved this turn, remove all +1/+1 counters from Ashling the Pilgrim, and it deals
        // that much damage to each creature and each player. The removal snapshots the removed count
        // as the entry's event value, which the mass damage reads back as "that much".
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new ConditionalEffect(new NthAbilityResolutionThisTurn(3),
                                new RemoveAllCountersFromSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        new ConditionalEffect(new NthAbilityResolutionThisTurn(3),
                                new MassDamageEffect(new EventValue(), true))
                ),
                "{1}{R}: Put a +1/+1 counter on Ashling the Pilgrim. If this is the third time this ability has resolved this turn, remove all +1/+1 counters from Ashling the Pilgrim, and it deals that much damage to each creature and each player."
        ));
    }
}
