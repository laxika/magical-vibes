package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FragmentOfKonda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.EachPlayerGainsControlOfOwnedPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "12")
public class TheFallOfLordKonda extends Card {

    public TheFallOfLordKonda() {
        setBackFaceCard(new FragmentOfKonda());

        PermanentPredicate chapterOneFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentMinManaValuePredicate(4),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ExileTargetPermanentEffect(chapterOneFilter));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(
                new SagaChapterTargetGroup(new PermanentPredicateTargetFilter(
                        chapterOneFilter,
                        "Target must be a creature an opponent controls with mana value 4 or greater"), 1, 1)));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new EachPlayerGainsControlOfOwnedPermanentsMatchingEffect(new PermanentTruePredicate()));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "FragmentOfKonda";
    }
}
