package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.OKagachiMadeManifest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentsThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "227")
public class TheKamiWar extends Card {

    public TheKamiWar() {
        setBackFaceCard(new OKagachiMadeManifest());

        var chapterOneTarget = TargetFilters.nonlandPermanentAnOpponentControls();
        addTrigger(Triggers.chapter(1),
                new ExileTargetPermanentEffect(chapterOneTarget.predicate()));
        setSagaChapterTargetFilter(1, Set.of(chapterOneTarget));

        var chapterTwoTarget = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                "Target must be another nonland permanent");
        addTrigger(Triggers.chapter(2),
                new ReturnTargetPermanentsThenEffect(
                        new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT),
                        chapterTwoTarget.predicate(), true));
        setSagaChapterTargetGroups(2,
                List.of(new SagaChapterTargetGroup(chapterTwoTarget, 0, 1)));

        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "OKagachiMadeManifest";
    }
}
