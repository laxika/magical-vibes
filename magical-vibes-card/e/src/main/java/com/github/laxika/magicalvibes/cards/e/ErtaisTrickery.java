package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryKickedPredicate;

@CardRegistration(set = "PLS", collectorNumber = "24")
public class ErtaisTrickery extends Card {

    public ErtaisTrickery() {
        addSpellEffect(new ConditionalEffect(
                new TargetSpellMatches(new StackEntryKickedPredicate()),
                new CounterSpellEffect()));
    }
}
