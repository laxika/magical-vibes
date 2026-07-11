package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySubtypeInPredicate;

import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "62")
public class FaerieTrickery extends Card {

    public FaerieTrickery() {
        // Counter target non-Faerie spell. If countered this way, exile it instead.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(new StackEntrySubtypeInPredicate(Set.of(CardSubtype.FAERIE))),
                "Target must be a non-Faerie spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellAndExileEffect());
    }
}
