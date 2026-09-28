package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "70")
@CardRegistration(set = "FIC", collectorNumber = "123")
public class SphereGrid extends Card {

    public SphereGrid() {
        // Whenever a creature you control deals combat damage to a player, put a +1/+1 counter on that creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        new PutCountersOnSourceEffect(1, 1, 1),
                        true));

        // Creatures you control with +1/+1 counters on them have reach and trample.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.REACH, Keyword.TRAMPLE),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
