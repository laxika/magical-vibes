package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CSP", collectorNumber = "43")
public class RimewindCryomancer extends Card {

    public RimewindCryomancer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new CounterSpellEffect()),
                "{1}, {T}: Counter target activated ability. Activate only if you control four or more snow permanents.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.ACTIVATED_ABILITY)),
                        "Target must be an activated ability."
                )
        ).withRequiredControlledPermanents(
                new PermanentHasSupertypePredicate(CardSupertype.SNOW),
                4,
                "four or more snow permanents"));
    }
}
