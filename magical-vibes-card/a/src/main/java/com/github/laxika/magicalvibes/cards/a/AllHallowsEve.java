package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.SourceCardInExileWithScreamCounter;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasCounterInExilePredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "88")
@CardRegistration(set = "ME3", collectorNumber = "57")
public class AllHallowsEve extends Card {

    public AllHallowsEve() {
        var scream = new CardHasCounterInExilePredicate(CounterType.SCREAM);
        addSpellEffect(ExileSpellEffect.withScreamCounters(2));
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.EXILE),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInExileWithScreamCounter()),
                SequenceEffect.of(
                        new RemoveCountersFromExiledCardsEffect(Subjects.exiledCards(Subjects.you(),
                                new CardAllOfPredicate(List.of(new CardIsSelfPredicate(), scream))), CounterType.SCREAM),
                        new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.exiledCards(Subjects.you(),
                                new CardAllOfPredicate(List.of(new CardIsSelfPredicate(), new CardNotPredicate(scream))))),
                        new ConditionalEffect(new HappenedThisWay(StepKind.PUT_INTO_GRAVEYARD),
                                new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                                        Integer.MAX_VALUE, new CardTypePredicate(CardType.CREATURE)))));
    }
}
