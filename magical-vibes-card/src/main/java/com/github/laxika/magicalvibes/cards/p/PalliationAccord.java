package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "122")
public class PalliationAccord extends Card {

    public PalliationAccord() {
        // Whenever a creature an opponent controls becomes tapped, put a palliation counter on this enchantment.
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentIsCreaturePredicate(),
                new PutCountersOnSelfEffect(CounterType.PALLIATION)));

        // Remove a palliation counter from this enchantment: Prevent the next 1 damage that would
        // be dealt to you this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PALLIATION),
                        PreventDamageEffect.nextToController(1)
                ),
                "Remove a palliation counter from this enchantment: Prevent the next 1 damage that would be dealt to you this turn."
        ));
    }
}
