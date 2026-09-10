package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "232")
public class SkeletalSwarming extends Card {

    public SkeletalSwarming() {
        PermanentPredicate skeleton = new PermanentHasSubtypePredicate(CardSubtype.SKELETON);
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, skeleton));

        PermanentCount otherSkeletons = new PermanentCount(skeleton, CountScope.CONTROLLER, true);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                otherSkeletons, new Fixed(0), GrantScope.OWN_CREATURES, skeleton, true));

        addEffect(EffectSlot.STATIC, new MatchingCreaturesMustAttackEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                skeleton,
                new PermanentControlledBySourceControllerPredicate()))));

        CreateTokenEffect skeletonToken = new CreateTokenEffect(
                1, "Skeleton", 1, 1, CardColor.BLACK, List.of(CardSubtype.SKELETON),
                Set.of(), Set.of(), true);
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalReplacementEffect(
                new Morbid(), skeletonToken, skeletonToken.withAmount(2)));
    }
}
