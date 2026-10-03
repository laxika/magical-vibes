package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "BRC", collectorNumber = "22")
public class TheBrothersWar extends Card {

    public TheBrothersWar() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, CreateTokenEffect.ofPowerstoneToken(new Fixed(2)));

        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");
        target(anyPlayer, 2, 2).addEffect(EffectSlot.SAGA_CHAPTER_II,
                new TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect(0));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_II,
                List.of(new SagaChapterTargetGroup(anyPlayer, 2, 2)));

        var anyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a creature, planeswalker, battle, or player.");
        var artifactCount = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        target(anyTarget).addEffect(EffectSlot.SAGA_CHAPTER_III,
                new DealDamageToAnyTargetEffect(artifactCount, false, false, 1, null));
        target(anyTarget).addEffect(EffectSlot.SAGA_CHAPTER_III,
                new DealDamageToAnyTargetEffect(artifactCount, false, false, 2, null));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_III, List.of(
                new SagaChapterTargetGroup(anyTarget, 1, 1),
                new SagaChapterTargetGroup(anyTarget, 1, 1)));
    }
}
