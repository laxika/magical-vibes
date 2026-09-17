package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.j.JurassicPark;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "REX", collectorNumber = "7")
public class WelcomeToJurassicPark extends Card {

    public WelcomeToJurassicPark() {
        setBackFaceCard(new JurassicPark());

        PermanentPredicate chapterOneFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        PermanentPredicateTargetFilter chapterOneTarget = new PermanentPredicateTargetFilter(
                chapterOneFilter, "Target must be a noncreature artifact an opponent controls");
        target(chapterOneTarget, 0, 99)
                .addEffect(EffectSlot.SAGA_CHAPTER_I, new AnimatePermanentsEffect(
                        new Fixed(0), new Fixed(4),
                        List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), null,
                        Set.of(CardType.CREATURE), GrantScope.TARGET,
                        EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD, chapterOneFilter));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(chapterOneTarget));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);

        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                1, "Dinosaur", 3, 3, CardColor.GREEN, null,
                List.of(CardSubtype.DINOSAUR), Set.of(Keyword.TRAMPLE), Set.of(Keyword.HASTE)));

        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new DestroyAllPermanentsEffect(new PermanentHasSubtypePredicate(CardSubtype.WALL)));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "JurassicPark";
    }
}
