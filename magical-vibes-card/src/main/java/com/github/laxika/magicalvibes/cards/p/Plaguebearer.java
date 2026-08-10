package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.CardColor;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EXO", collectorNumber = "71")
public class Plaguebearer extends Card {

    public Plaguebearer() {
        PermanentPredicate nonblackCreatureWithManaValueX = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK))),
                new PermanentManaValueEqualsXPredicate()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{X}{B}",
                List.of(new DestroyTargetPermanentEffect()),
                "{X}{X}{B}: Destroy target nonblack creature with mana value X.",
                new PermanentPredicateTargetFilter(
                        nonblackCreatureWithManaValueX,
                        "Target must be a nonblack creature with mana value X"
                )
        ));
    }
}
