package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;

@CardRegistration(set = "ONE", collectorNumber = "67")
public class RejectImperfection extends Card {

    public RejectImperfection() {
        // "Counter target spell. If that spell's mana value was 3 or less, proliferate." The counter
        // resolves first; the mana value is then judged from the spell's last known information.
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new ConditionalEffect(
                new TargetSpellMatches(new StackEntryMaxManaValuePredicate(3)),
                new ProliferateEffect()));
    }
}
