package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HML", collectorNumber = "78")
public class OrcishMine extends Card {

    public OrcishMine() {
        // Enchant land
        target(new PermanentPredicateTargetFilter(new PermanentIsLandPredicate(), "Target must be a land"));
        // This Aura enters with three ore counters on it.
        addStaticEffect(new EnterWithCountersEffect(CounterType.ORE, new Fixed(3)));
        // At the beginning of your upkeep and whenever enchanted land becomes tapped, remove an ore
        // counter from this Aura.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), RemoveCountersEffect.fromSelf(CounterType.ORE, 1));
        addTrigger(Triggers.enchantedPermanentBecomesTapped(), RemoveCountersEffect.fromSelf(CounterType.ORE, 1));
        // When the last ore counter is removed, destroy enchanted land and this Aura deals 2 damage to
        // that land's controller. A state trigger, so it fires however the last counter was removed.
        addTrigger(Triggers.sourceState(new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.ORE))),
                new DealDamageToEnchantedPermanentControllerEffect(2),
                new DestroyTargetPermanentEffect(Subjects.attached()));
    }
}
