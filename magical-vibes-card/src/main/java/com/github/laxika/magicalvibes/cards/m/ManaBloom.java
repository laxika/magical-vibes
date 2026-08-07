package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "130")
public class ManaBloom extends Card {

    public ManaBloom() {
        // This enchantment enters with X charge counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.CHARGE, new XValue()));

        // Remove a charge counter from this enchantment: Add one mana of any color.
        // Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.CHARGE),
                        new AwardAnyColorManaEffect()
                ),
                "Remove a charge counter from this enchantment: Add one mana of any color. Activate only once each turn.",
                1));

        // At the beginning of your upkeep, if this enchantment has no charge counters on it,
        // return it to its owner's hand.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.CHARGE)),
                ReturnToHandEffect.self()));
    }
}
