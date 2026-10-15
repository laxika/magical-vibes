package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EsperTerra extends Card {

    private static final PermanentPredicate NONLEGENDARY_ENCHANTMENT_YOU_CONTROL =
            new PermanentAllOfPredicate(List.of(
                    new PermanentIsEnchantmentPredicate(),
                    new PermanentControlledBySourceControllerPredicate(),
                    new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))));

    public EsperTerra() {
        addCopyChapter(EffectSlot.SAGA_CHAPTER_I);
        addCopyChapter(EffectSlot.SAGA_CHAPTER_II);
        addCopyChapter(EffectSlot.SAGA_CHAPTER_III);

        addEffect(EffectSlot.SAGA_CHAPTER_IV, new AwardManaEffect(ManaColor.WHITE, 2));
        addEffect(EffectSlot.SAGA_CHAPTER_IV, new AwardManaEffect(ManaColor.BLUE, 2));
        addEffect(EffectSlot.SAGA_CHAPTER_IV, new AwardManaEffect(ManaColor.BLACK, 2));
        addEffect(EffectSlot.SAGA_CHAPTER_IV, new AwardManaEffect(ManaColor.RED, 2));
        addEffect(EffectSlot.SAGA_CHAPTER_IV, new AwardManaEffect(ManaColor.GREEN, 2));
        addEffect(EffectSlot.SAGA_CHAPTER_IV, new ExileSelfAndReturnTransformedEffect());
    }

    private void addCopyChapter(EffectSlot chapter) {
        addEffect(chapter, copyEnchantmentEffect());
        setSagaChapterTargetGroups(chapter, List.of(new SagaChapterTargetGroup(
                new PermanentPredicateTargetFilter(
                        NONLEGENDARY_ENCHANTMENT_YOU_CONTROL,
                        "Target must be a nonlegendary enchantment you control"),
                1,
                1)));
    }

    private static CreateTokenCopyOfTargetPermanentEffect copyEnchantmentEffect() {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(),
                Set.of(),
                null,
                null,
                Map.of(),
                true,
                false,
                true,
                false,
                false,
                false,
                null,
                Set.of(),
                false,
                Map.of(EffectSlot.ON_ENTER_BATTLEFIELD, List.of(new ConditionalEffect(
                        new SourceHasSubtype(CardSubtype.SAGA),
                        SequenceEffect.of(
                                new ChooseNumberEffect(0, 3),
                                new PutCountersOnSourceCardEffect(
                                        CounterType.LORE, new ChosenNumberOnSource()))))));
    }
}
