package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.DuringCombat;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "41")
public class DjinnOfInfiniteDeceits extends Card {

    private static final PermanentPredicate NONLEGENDARY_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))));

    public DjinnOfInfiniteDeceits() {
        var targetFilter = new PermanentPredicateTargetFilter(
                NONLEGENDARY_CREATURE, "Target must be a nonlegendary creature");

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(ExchangeControlOfTargetPermanentsEffect.forTwoTargetsInOneGroup(
                        NONLEGENDARY_CREATURE)),
                "{T}: Exchange control of two target nonlegendary creatures. You can't activate this ability during combat.",
                List.of(targetFilter, targetFilter),
                2,
                2
        ).withActivationCondition(
                new NotCondition(new DuringCombat()),
                "You can't activate this ability during combat."));
    }
}
