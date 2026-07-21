package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "3")
public class AngelOfCondemnation extends Card {

    public AngelOfCondemnation() {
        // Flying, vigilance — auto-loaded from Scryfall.

        // {2}{W}, {T}: Exile another target creature. Return that card to the battlefield under its
        // owner's control at the beginning of the next end step. (Temporary blink.)
        addActivatedAbility(new ActivatedAbility(
                true, "{2}{W}",
                List.of(FlickerEffect.exileTargetReturnAtEndStep()),
                "{2}{W}, {T}: Exile another target creature. Return that card to the battlefield under its owner's control at the beginning of the next end step.",
                anotherCreatureFilter()
        ));

        // {2}{W}, {T}, Exert this creature: Exile another target creature until this creature leaves
        // the battlefield. Exert is modeled as an added SkipNextUntapEffect cost.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}{W}",
                List.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF),
                        new ExileTargetPermanentUntilSourceLeavesEffect()
                ),
                "{2}{W}, {T}, Exert Angel of Condemnation: Exile another target creature until this creature leaves the battlefield.",
                anotherCreatureFilter()
        ));
    }

    private static PermanentPredicateTargetFilter anotherCreatureFilter() {
        return new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another creature"
        );
    }
}
