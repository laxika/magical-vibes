package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.VoteForInnocentOrGuiltyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "29")
@CardRegistration(set = "WHO", collectorNumber = "634")
public class TrialOfATimeLord extends Card {

    private static final PermanentPredicate OPPONENT_NONTOKEN_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsTokenPredicate()),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

    public TrialOfATimeLord() {
        addChapterExile(1);
        addChapterExile(2);
        addChapterExile(3);
        addTrigger(Triggers.chapter(4), new VoteForInnocentOrGuiltyEffect());
    }

    private void addChapterExile(int chapter) {
        addTrigger(Triggers.chapter(chapter), new ExileTargetPermanentUntilSourceLeavesEffect(
                false, OPPONENT_NONTOKEN_CREATURE));
        setSagaChapterTargetFilter(chapter, Set.of(new PermanentPredicateTargetFilter(
                OPPONENT_NONTOKEN_CREATURE,
                "Must target a nontoken creature an opponent controls")));
    }
}
