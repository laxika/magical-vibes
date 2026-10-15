package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EXO", collectorNumber = "65")
public class KeeperOfTheDead extends Card {

    public KeeperOfTheDead() {
        PermanentPredicate nonblackCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(DestroyTargetPermanentEffect.forTargetGroup(1)),
                "{B}, {T}: Choose target opponent who has at least two fewer creature cards in their graveyard than you do "
                        + "as you activate this ability. Destroy target nonblack creature that player controls.",
                new PermanentPredicateTargetFilter(nonblackCreature, "Target must be a nonblack creature"),
                null,
                null,
                null,
                List.<TargetFilter>of(
                        new PlayerPredicateTargetFilter(
                                new PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate(2),
                                "Target opponent must have at least two fewer creature cards in their graveyard than you"
                        ),
                        new PermanentPredicateTargetFilter(nonblackCreature, "Target must be a nonblack creature")
                ),
                2,
                2
        ).withMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET));
    }
}
