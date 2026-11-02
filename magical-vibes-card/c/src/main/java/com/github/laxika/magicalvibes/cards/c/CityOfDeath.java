package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "99")
@CardRegistration(set = "WHO", collectorNumber = "704")
public class CityOfDeath extends Card {

    private static final PermanentPredicate TARGET = new PermanentAllOfPredicate(List.of(
            new PermanentIsTokenPredicate(),
            new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SAGA))));

    public CityOfDeath() {
        addTrigger(Triggers.chapter(1), CreateTokenEffect.ofTreasureToken(1));

        addCopyChapter(2);
        addCopyChapter(3);
        addCopyChapter(4);
        addCopyChapter(5);
        addCopyChapter(6);
    }

    private void addCopyChapter(int chapter) {
        var filter = new ControlledPermanentPredicateTargetFilter(
                TARGET, "Target must be a non-Saga token you control.");
        addTrigger(Triggers.chapter(chapter), new CreateTokenCopyOfTargetPermanentEffect());
        setSagaChapterTargetFilter(chapter, Set.of(filter));
        setSagaChapterTargetGroups(chapter, List.of(new SagaChapterTargetGroup(filter, 1, 1)));
    }
}
