package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryColoredManaSpentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "242")
public class VoidMirror extends Card {

    public VoidMirror() {
        // Whenever a player casts a spell, if no colored mana was spent to cast it, counter that spell. What was spent
        // can't change once the spell is cast, so the stack-entry filter judges the "if" as it would resolve too.
        addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryColoredManaSpentPredicate())),
                new CounterSpellEffect());
    }
}
