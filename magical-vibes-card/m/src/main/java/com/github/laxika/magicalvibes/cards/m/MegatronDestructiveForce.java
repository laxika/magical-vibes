package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/** Back face of Megatron, Tyrant. */
public class MegatronDestructiveForce extends Card {

    public MegatronDestructiveForce() {
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new NotControllerTurn()),
                new GrantCardTypeEffect(CardType.CREATURE, Subjects.source())));

        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(
                                SequenceEffect.of(
                                        DealDamageEffect.toTargetCreature(AmountThisWay.sacrificedManaValue())
                                                .excessToControllerInstead(),
                                        new ConditionalEffect(new EventValueAtLeast(1), new TransformSelfEffect()))))),
                "Sacrifice another artifact?"));
    }
}
