package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.OKagachiMadeManifest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "227")
public class TheKamiWar extends Card {

    public TheKamiWar() {
        setBackFaceCard(new OKagachiMadeManifest());

        var chapterOneTarget = TargetFilters.nonlandPermanentAnOpponentControls();
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new ExileTargetPermanentEffect(chapterOneTarget.predicate()));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(chapterOneTarget));

        var chapterTwoTarget = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                "Target must be another nonland permanent");
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new ReturnTargetPermanentsThenEffect(
                        new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT),
                        chapterTwoTarget.predicate(), true));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_II,
                List.of(new SagaChapterTargetGroup(chapterTwoTarget, 0, 1)));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "OKagachiMadeManifest";
    }
}
