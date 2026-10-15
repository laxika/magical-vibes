package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenDestroyEnchantedAtZeroEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "78")
public class OrcishMine extends Card {

    public OrcishMine() {
        // Enchant land
        target(new PermanentPredicateTargetFilter(new PermanentIsLandPredicate(), "Target must be a land"));
        // This Aura enters with three ore counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(CounterType.ORE, new Fixed(3)));
        // At the beginning of your upkeep and whenever enchanted land becomes tapped, remove an ore
        // counter from this Aura. When the last ore counter is removed, destroy enchanted land and this
        // Aura deals 2 damage to that land's controller.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterFromSourceThenDestroyEnchantedAtZeroEffect(CounterType.ORE, 2));
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                new RemoveCounterFromSourceThenDestroyEnchantedAtZeroEffect(CounterType.ORE, 2));
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.ORE)),
                List.of(new RemoveCounterFromSourceThenDestroyEnchantedAtZeroEffect(CounterType.ORE, 2)),
                "Orcish Mine's last ore counter ability"));
    }
}
