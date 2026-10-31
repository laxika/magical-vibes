package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetSpellManaSpentLessThanManaValue;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "EOE", collectorNumber = "83")
public class Unravel extends Card {

    public Unravel() {
        // Counter first, then draw (CR 608.2c). The condition reads the countered spell's mana spent
        // from its last-known record, so it still applies once the spell has left the stack.
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new ConditionalEffect(
                new TargetSpellManaSpentLessThanManaValue(), new DrawCardEffect()));
    }
}
