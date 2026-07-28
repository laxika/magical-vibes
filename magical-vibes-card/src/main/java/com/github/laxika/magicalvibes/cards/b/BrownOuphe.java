package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "228")
public class BrownOuphe extends Card {

    public BrownOuphe() {
        // "{1}{G}, {T}: Counter target activated ability from an artifact source."
        // The stack entry of an activated ability carries its source card, so an artifact source is
        // an ARTIFACT-typed card on an ACTIVATED_ABILITY entry. Mana abilities never use the stack,
        // so they are excluded automatically. Any player's ability is a legal target.
        addActivatedAbility(new ActivatedAbility(true, "{1}{G}",
                List.of(new CounterSpellEffect()),
                "{1}{G}, {T}: Counter target activated ability from an artifact source.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.ACTIVATED_ABILITY)),
                                new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT)))),
                        "Target must be an activated ability from an artifact source.")));
    }
}
