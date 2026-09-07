package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "124")
public class AnaBattlemage extends Card {

    public AnaBattlemage() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{U}"));
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.singlePayment(List.of("{1}{B}")));

        targetWhenKicked(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ), 0, 0, 1, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Kicked(), new DiscardEffect(3, DiscardRecipient.TARGET_PLAYER)));

        targetWithDynamicCount(new RepeatedAdditionalCostCount("{1}{B}"), new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTappedPredicate())
                )), "Target must be an untapped creature"), 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ConditionalEffect(new RepeatedAdditionalCostPaid("{1}{B}"),
                                new TapPermanentsEffect(TapUntapScope.TARGET)))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ConditionalEffect(new RepeatedAdditionalCostPaid("{1}{B}"),
                                new TargetCreatureDealsPowerDamageToControllerEffect()));
    }
}
