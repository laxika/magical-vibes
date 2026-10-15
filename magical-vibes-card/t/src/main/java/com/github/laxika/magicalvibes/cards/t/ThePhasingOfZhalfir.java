package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForEachDestroyedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutTargetPermanentWhileSourceControlledEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "59")
public class ThePhasingOfZhalfir extends Card {

    private static final PermanentPredicate ANOTHER_NONLAND_PERMANENT = new PermanentAllOfPredicate(List.of(
            new PermanentNotPredicate(new PermanentIsLandPredicate()),
            new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    public ThePhasingOfZhalfir() {
        addChapterPhaseOut(EffectSlot.SAGA_CHAPTER_I);
        addChapterPhaseOut(EffectSlot.SAGA_CHAPTER_II);

        addEffect(EffectSlot.SAGA_CHAPTER_III, new DestroyAllPermanentsEffect(
                new PermanentIsCreaturePredicate(),
                new CreateTokenForEachDestroyedPermanentControllerEffect(new CreateTokenEffect(
                        1,
                        "Phyrexian",
                        2,
                        2,
                        CardColor.BLACK,
                        List.of(CardSubtype.PHYREXIAN),
                        Set.of(),
                        Set.of()))));
    }

    private void addChapterPhaseOut(EffectSlot chapter) {
        addEffect(chapter, new PhaseOutTargetPermanentWhileSourceControlledEffect(
                ANOTHER_NONLAND_PERMANENT));
        setSagaChapterTargetGroups(chapter, List.of(new SagaChapterTargetGroup(
                new PermanentPredicateTargetFilter(
                        ANOTHER_NONLAND_PERMANENT,
                        "Target must be another nonland permanent"),
                1,
                1)));
    }
}
