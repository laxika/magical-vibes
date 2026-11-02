package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDealPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "205")
public class TheBearsOfLittjara extends Card {

    public TheBearsOfLittjara() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                "Shapeshifter", 2, 2, CardColor.BLUE,
                List.of(CardSubtype.SHAPESHIFTER), Set.of(Keyword.CHANGELING), Set.of()));

        addTrigger(Triggers.chapter(2),
                SetBasePowerToughnessEffect.indefinitely(4, 4));
        setSagaChapterTargetGroups(2, List.of(
                new SagaChapterTargetGroup(new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.SHAPESHIFTER),
                                new PermanentControlledBySourceControllerPredicate())),
                        "Target must be a Shapeshifter creature you control"),
                        0, Integer.MAX_VALUE)
        ));

        addTrigger(Triggers.chapter(3),
                ControlledCreaturesDealPowerDamageToTargetEffect.creatureOrPlaneswalker(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtLeastPredicate(4)))));
        setSagaChapterTargetGroups(3, List.of(
                new SagaChapterTargetGroup(new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsPlaneswalkerPredicate())),
                        "Target must be a creature or planeswalker"),
                        0, 1)
        ));
    }
}
