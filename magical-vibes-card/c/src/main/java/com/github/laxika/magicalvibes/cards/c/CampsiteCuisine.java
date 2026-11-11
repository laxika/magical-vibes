package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
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
                Set.of(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE), GrantScope.TARGETS);
        targetUpTo(new EventValue(), TargetFilters.attackingCreature(), 100)
                .addTrigger(Triggers.youAttack(), new MayEffect(
                        SequenceEffect.of(
                                new SacrificeAnyNumberOfPermanentsEffect(
                                        new PermanentHasSubtypePredicate(CardSubtype.FOOD)),
                                new ConditionalEffect(
                                        new EventValueAtLeast(1),
                                        new QueueReflexiveAbilityEffect(
                                                SequenceEffect.of(
                                                        new BoostTargetCreatureEffect(3, 3),
                                                        grantKeywords)))),
                        "Sacrifice one or more Foods?"));
    }
}
