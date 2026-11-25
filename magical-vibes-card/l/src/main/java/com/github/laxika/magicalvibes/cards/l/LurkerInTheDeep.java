package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YDSK", collectorNumber = "6")
public class LurkerInTheDeep extends Card {

    public LurkerInTheDeep() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{U}{U}"))));

        SeekEffect seekNonland = new SeekEffect(new CardNotPredicate(new CardTypePredicate(CardType.LAND)));
        addTrigger(Triggers.enters(ObjectRelation.THIS), seekNonland);
        addTrigger(Triggers.attacks(), seekNonland);
        addTrigger(Triggers.youSeek(), TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn()),
                SequenceEffect.of(
                        ConjureCardsEffect.duplicateOf(Subjects.triggeringCardsInHand(), ConjureDestination.intoHand()),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CONJURE)).manifested()));

        addStaticEffect(new ConditionalEffect(
                new CastForAlternateCost(),
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(3))));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                new CastForAlternateCost(),
                                new SourceCounterThreshold(1, CounterType.TIME)))),
                RemoveCountersEffect.fromSelf(CounterType.TIME, 1));
        addStaticEffect(new ConditionalEffect(
                new AllOf(List.of(
                        new CastForAlternateCost(),
                        new SourceCounterThreshold(1, CounterType.TIME))),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), Subjects.source())));
    }
}
