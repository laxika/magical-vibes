package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesNameWithControlledTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "200")
public class TheApprenticesFolly extends Card {

    private static final PermanentPredicate COPY_TARGET = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsTokenPredicate()),
            new PermanentNotPredicate(new PermanentSharesNameWithControlledTokenPredicate())
    ));

    public TheApprenticesFolly() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, copyEffect());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(copyTargetGroup()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, copyEffect());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_II, List.of(copyTargetGroup()));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new SacrificeEachMatchingPermanentEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.REFLECTION),
                        new PermanentControlledBySourceControllerPredicate()))));
    }

    private CreateTokenCopyOfTargetPermanentEffect copyEffect() {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(CardSubtype.REFLECTION), Set.of(), null, null, Map.of(),
                true, false, false, false, false, false, null, Set.of(), true);
    }

    private SagaChapterTargetGroup copyTargetGroup() {
        TargetFilter targetFilter = new ControlledPermanentPredicateTargetFilter(
                COPY_TARGET, "Target must be a nontoken creature you control without the same name as a token you control");
        return new SagaChapterTargetGroup(targetFilter, 1, 1);
    }
}
