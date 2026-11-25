package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "212")
@CardRegistration(set = "THB", collectorNumber = "269")
public class AthreosShroudVeiled extends Card {

    public AthreosShroudVeiled() {
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new DevotionToColorsAtLeast(Set.of(ManaColor.WHITE, ManaColor.BLACK), 7)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), Subjects.source())));

        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        target(new PermanentPredicateTargetFilter(anotherCreature, "Target must be another creature"))
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        PutCountersEffect.withTargetRestriction(
                                CounterType.COIN, 1, anotherCreature));

        PermanentPredicate coinCounter = new PermanentHasCountersPredicate(CounterType.COIN);
        addTrigger(Triggers.creatureDies(coinCounter, ObjectRelation.ANY),
                new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards())
                        .underControlOf(Subjects.you()));
        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD),
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, coinCounter)), ObjectRelation.ANY),
                new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards())
                        .underControlOf(Subjects.you()));
    }
}
