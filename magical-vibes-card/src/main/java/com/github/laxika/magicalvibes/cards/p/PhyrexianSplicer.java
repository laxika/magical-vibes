package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MoveKeywordFromTargetToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * "{2}, {T}, Choose flying, first strike, trample, or shadow: Until end of turn, target creature
 * with the chosen ability loses it and another target creature gains it."
 *
 * <p>The keyword is chosen as the ability is activated, before targets are declared, and it
 * restricts the first target ("target creature with the chosen ability"). The engine has no
 * activation-time keyword prompt, so the choice is modelled as four separate activated abilities —
 * one per keyword — each carrying the matching first-target restriction. Choosing which ability to
 * activate is exactly the choice the oracle text asks for, and the restriction is enforced at
 * announcement as it should be.
 */
@CardRegistration(set = "TMP", collectorNumber = "303")
public class PhyrexianSplicer extends Card {

    public PhyrexianSplicer() {
        addSplicerAbility(Keyword.FLYING, "flying");
        addSplicerAbility(Keyword.FIRST_STRIKE, "first strike");
        addSplicerAbility(Keyword.TRAMPLE, "trample");
        addSplicerAbility(Keyword.SHADOW, "shadow");
    }

    private void addSplicerAbility(Keyword keyword, String label) {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new MoveKeywordFromTargetToTargetEffect(keyword)),
                "{2}, {T}: Until end of turn, target creature with " + label
                        + " loses it and another target creature gains it.",
                List.of(
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasKeywordPredicate(keyword))),
                                "First target must be a creature with " + label),
                        new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                                "Second target must be a creature")
                ),
                2,
                2
        ));
    }
}
