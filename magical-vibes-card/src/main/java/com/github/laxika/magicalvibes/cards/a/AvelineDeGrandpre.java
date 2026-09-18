package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "ACR", collectorNumber = "40")
@CardRegistration(set = "ACR", collectorNumber = "135")
public class AvelineDeGrandpre extends Card {

    public AvelineDeGrandpre() {
        // Whenever a creature you control with deathtouch deals combat damage to a player,
        // put that many +1/+1 counters on that creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()),
                        true));
    }
}
