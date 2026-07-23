package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "26")
public class Fylgja extends Card {

    public Fylgja() {
        // Enchant creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));

        // This Aura enters with four healing counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.HEALING, new Fixed(4)));

        // Remove a healing counter from this Aura: Prevent the next 1 damage that would be
        // dealt to enchanted creature this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.HEALING),
                        PreventDamageEffect.nextToEnchanted(1)
                ),
                "Remove a healing counter from this Aura: Prevent the next 1 damage that would be dealt to enchanted creature this turn."
        ));

        // {2}{W}: Put a healing counter on this Aura.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new PutCountersOnSelfEffect(CounterType.HEALING)),
                "{2}{W}: Put a healing counter on this Aura."
        ));
    }
}
