package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addTrigger(Triggers.chapter(1), new ExileTargetPermanentEffect(chapterOneFilter));
        setSagaChapterTargetFilter(1, Set.of(
                new PermanentPredicateTargetFilter(
                        chapterOneFilter,
                        "Target must be an artifact, enchantment, or tapped creature an opponent controls")));

        addTrigger(Triggers.chapter(2),
                new RequirePaymentToAttackControllerUntilNextTurnEffect(2));
        addTrigger(Triggers.chapter(3),
                new RequirePaymentToAttackControllerUntilNextTurnEffect(2));

        addTrigger(Triggers.chapter(4), CreateTokenEffect.ofTreasureToken(
                new OpponentsWithCreaturePowerAtLeast(4)));
    }
}
