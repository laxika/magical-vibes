package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapXPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "192")
public class AryelKnightOfWindgrace extends Card {

    public AryelKnightOfWindgrace() {
        // {2}{W}, {T}: Create a 2/2 white Knight creature token with vigilance.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}{W}",
                List.of(new CreateCreatureTokenEffect(
                        "Knight", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.KNIGHT),
                        Set.of(Keyword.VIGILANCE), Set.of()
                )),
                "{2}{W}, {T}: Create a 2/2 white Knight creature token with vigilance."
        ));

        // {B}, {T}, Tap X untapped Knights you control: Destroy target creature with power X or less.
        addActivatedAbility(new ActivatedAbility(
                true, "{B}",
                List.of(
                        new TapXPermanentsCost(new PermanentHasSubtypePredicate(CardSubtype.KNIGHT), true),
                        new DestroyTargetPermanentEffect()
                ),
                "{B}, {T}, Tap X untapped Knights you control: Destroy target creature with power X or less.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtMostXPredicate()
                        )),
                        "Target must be a creature with power X or less"
                )
        ));
    }
}
