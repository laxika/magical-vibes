package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "232")
public class TheTricksterGodsHeist extends Card {

    private static final PermanentPredicate CREATURE = new PermanentIsCreaturePredicate();
    private static final PermanentPredicate NONBASIC_NONCREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC)),
            new PermanentNotPredicate(new PermanentIsCreaturePredicate())
    ));

    public TheTricksterGodsHeist() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new MayEffect(
                new ExchangeControlOfTargetPermanentsEffect(CREATURE, false, false),
                "Exchange control of the two target creatures?"));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 1, 1),
                new SagaChapterTargetGroup(TargetFilters.creature(), 1, 1)
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new MayEffect(
                new ExchangeControlOfTargetPermanentsEffect(
                        NONBASIC_NONCREATURE, false, false, false, false, false, false, true, null, false),
                "Exchange control of the two target nonbasic, noncreature permanents?"));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_II, List.of(
                new SagaChapterTargetGroup(new PermanentPredicateTargetFilter(
                        NONBASIC_NONCREATURE,
                        "Target must be a nonbasic, noncreature permanent"), 1, 1),
                new SagaChapterTargetGroup(new PermanentPredicateTargetFilter(
                        NONBASIC_NONCREATURE,
                        "Target must be a nonbasic, noncreature permanent"), 1, 1)
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new GainLifeEffect(3));
    }
}
