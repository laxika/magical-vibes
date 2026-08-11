package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "49")
public class Gainsay extends Card {

    public Gainsay() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryColorInPredicate(Set.of(CardColor.BLUE)),
                "Target spell must be blue."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
