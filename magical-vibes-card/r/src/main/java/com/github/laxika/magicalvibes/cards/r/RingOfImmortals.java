package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySubtypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LEG", collectorNumber = "293")
public class RingOfImmortals extends Card {

    public RingOfImmortals() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new CounterSpellEffect()),
                "{3}, {T}: Counter target instant or Aura spell that targets a permanent you control.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryAnyOfPredicate(List.of(
                                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL)),
                                        new StackEntryAllOfPredicate(List.of(
                                                new StackEntryTypeInPredicate(Set.of(StackEntryType.ENCHANTMENT_SPELL)),
                                                new StackEntrySubtypeInPredicate(Set.of(CardSubtype.AURA))
                                        ))
                                )),
                                new StackEntryTargetsYourPermanentPredicate()
                        )),
                        "Target must be an instant or Aura spell that targets a permanent you control."
                )
        ));
    }
}
