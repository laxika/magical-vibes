package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringPermanentToBattlefieldWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
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
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));

        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        target(new PermanentPredicateTargetFilter(anotherCreature, "Target must be another creature"))
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                CounterType.COIN, 1, anotherCreature));

        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new ReturnTriggeringPermanentToBattlefieldWithCounterEffect(CounterType.COIN, Zone.GRAVEYARD));
        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), Triggers.CREATURE, ObjectRelation.ANY),
                new ReturnTriggeringPermanentToBattlefieldWithCounterEffect(CounterType.COIN, Zone.EXILE));
    }
}
