package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySubtypeInPredicate;

import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "67")
public class HisokasDefiance extends Card {

    public HisokasDefiance() {
        // Counter target Spirit or Arcane spell.
        target(new StackEntryPredicateTargetFilter(
                new StackEntrySubtypeInPredicate(Set.of(CardSubtype.SPIRIT, CardSubtype.ARCANE)),
                "Target must be a Spirit or Arcane spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
