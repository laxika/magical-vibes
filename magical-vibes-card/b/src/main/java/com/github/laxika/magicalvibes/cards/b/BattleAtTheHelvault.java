package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "719")
public class BattleAtTheHelvault extends Card {

    public BattleAtTheHelvault() {
        PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SAGA))));
        SagaChapterTargetGroup targetGroup = new SagaChapterTargetGroup(
                new PermanentPredicateTargetFilter(
                        targetPredicate, "Target must be a non-Saga, nonland permanent"),
                0, 99);

        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        addTrigger(Triggers.chapter(1),
                new ExileTargetPermanentUntilSourceLeavesEffect(false, targetPredicate));
        setSagaChapterTargetGroups(1, List.of(targetGroup));

        addTrigger(Triggers.chapter(2),
                new ExileTargetPermanentUntilSourceLeavesEffect(false, targetPredicate));
        setSagaChapterTargetGroups(2, List.of(targetGroup));

        addTrigger(Triggers.chapter(3), new CreateTokenEffect(
                CardType.CREATURE, 1, "Avacyn", 8, 8, CardColor.WHITE, null,
                List.of(CardSubtype.ANGEL),
                Set.of(Keyword.FLYING, Keyword.VIGILANCE, Keyword.INDESTRUCTIBLE), Set.of(),
                false, false, List.of(), List.of(), false, false, true, 0, Set.of()));
    }
}
