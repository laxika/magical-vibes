package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "88")
public class Helldozer extends Card {

    public Helldozer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}{B}{B}",
                List.of(
                        new ConditionalEffect(
                                new TargetPermanentMatches(
                                        new PermanentNotPredicate(
                                                new PermanentHasSupertypePredicate(CardSupertype.BASIC))),
                                new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT)),
                        new DestroyTargetPermanentEffect()),
                "{B}{B}{B}, {T}: Destroy target land. If that land was nonbasic, untap this creature.",
                TargetFilters.land()
        ));
    }
}
