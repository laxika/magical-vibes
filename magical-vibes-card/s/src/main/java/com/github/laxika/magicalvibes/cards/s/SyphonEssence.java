package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "84")
@CardRegistration(set = "DBL", collectorNumber = "351")
public class SyphonEssence extends Card {

    public SyphonEssence() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.CREATURE_SPELL,
                        StackEntryType.PLANESWALKER_SPELL
                )),
                "Target must be a creature or planeswalker spell."
        ))
                .addSpellEffect(new CounterSpellEffect())
                .addSpellEffect(CreateTokenEffect.ofBloodToken(1));
    }
}
