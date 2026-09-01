package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHR", collectorNumber = "73")
@CardRegistration(set = "LEG", collectorNumber = "220")
public class AyeshaTanaka extends Card {

    public AyeshaTanaka() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CounterUnlessPaysEffect("{W}")),
                "{T}: Counter target activated ability from an artifact source unless that ability's controller pays {W}.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.ACTIVATED_ABILITY)),
                                new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT)))),
                        "Target must be an activated ability from an artifact source."
                )
        ));
    }
}
