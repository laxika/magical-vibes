package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTwoExiledCardsWithSourceToBattlefieldAndBottomRestEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "26")
@CardRegistration(set = "PIP", collectorNumber = "554")
public class Vault13DwellersJourney extends Card {

    public Vault13DwellersJourney() {
        PermanentPredicate chapterOnePredicate = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate())),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        var chapterOneTarget = new PermanentPredicateTargetFilter(
                chapterOnePredicate, "Target must be another enchantment or creature.");
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(chapterOneTarget, 0, 99)
                .addEffect(EffectSlot.SAGA_CHAPTER_I,
                        new ExileTargetPermanentUntilSourceLeavesEffect());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I,
                List.of(new SagaChapterTargetGroup(chapterOneTarget, 0, 99)));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new ScryEffect(2));

        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new ReturnTwoExiledCardsWithSourceToBattlefieldAndBottomRestEffect());
    }
}
