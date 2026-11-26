package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "464")
public class CampsiteCuisine extends Card {

    public CampsiteCuisine() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofFoodToken(1));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                CreateTokenEffect.ofFoodToken(1));

        var grantKeywords = new GrantKeywordEffect(
                Set.of(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE), Subjects.triggeringSpellTargets());
        targetUpTo(new AmountThisWay(StepKind.SACRIFICE), TargetFilters.attackingCreature(), 100)
                .addTrigger(Triggers.youAttack(), new MayEffect(
                        SequenceEffect.of(
                                SacrificePermanentsEffect.anyNumber(
                                        new PermanentHasSubtypePredicate(CardSubtype.FOOD), Subjects.you()),
                                new ConditionalEffect(
                                        HappenedThisWay.sacrificed(),
                                        new QueueReflexiveAbilityEffect(
                                                SequenceEffect.of(
                                                        BoostEffect.target(3, 3),
                                                        grantKeywords)))),
                        "Sacrifice one or more Foods?"));
    }
}
