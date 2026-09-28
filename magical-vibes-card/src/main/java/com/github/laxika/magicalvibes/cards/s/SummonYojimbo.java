package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithCreaturePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackControllerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "28")
@CardRegistration(set = "FIC", collectorNumber = "196")
public class SummonYojimbo extends Card {

    public SummonYojimbo() {
        PermanentPredicate opponentPermanent = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        PermanentPredicate artifactOrEnchantmentOrTappedCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()))));
        PermanentPredicate chapterOneFilter = new PermanentAllOfPredicate(List.of(
                opponentPermanent, artifactOrEnchantmentOrTappedCreature));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ExileTargetPermanentEffect(chapterOneFilter));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(
                new PermanentPredicateTargetFilter(
                        chapterOneFilter,
                        "Target must be an artifact, enchantment, or tapped creature an opponent controls")));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new RequirePaymentToAttackControllerUntilNextTurnEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new RequirePaymentToAttackControllerUntilNextTurnEffect(2));

        addEffect(EffectSlot.SAGA_CHAPTER_IV, CreateTokenEffect.ofTreasureToken(
                new OpponentsWithCreaturePowerAtLeast(4)));
    }
}
