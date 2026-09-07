package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnFrontEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

public class TheTrueScriptures extends Card {

    public TheTrueScriptures() {
        var chapterOneTarget = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsPlaneswalkerPredicate())),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                "Target must be a creature or planeswalker an opponent controls.");
        target(chapterOneTarget, 0, 99)
                .addEffect(EffectSlot.SAGA_CHAPTER_I, new DestroyEachTargetPermanentEffect());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(chapterOneTarget));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);

        addEffect(EffectSlot.SAGA_CHAPTER_II, new DiscardEffect(3, DiscardRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new MillEffect(3, MillRecipient.EACH_OPPONENT));

        addEffect(EffectSlot.SAGA_CHAPTER_III, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .returnAll(true)
                .build());
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnFrontEffect());
    }
}
