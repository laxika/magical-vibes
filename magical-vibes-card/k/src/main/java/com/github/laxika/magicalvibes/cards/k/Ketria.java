package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "55")
public class Ketria extends Card {

    public Ketria() {
        ChooseOneForTargetCreatureEffect counterChoice = new ChooseOneForTargetCreatureEffect(List.of(
                counterMode("Put a vigilance counter on it", CounterType.VIGILANCE),
                counterMode("Put a menace counter on it", CounterType.MENACE),
                counterMode("Put a trample counter on it", CounterType.TRAMPLE)));
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.planeswalkToThis(), counterChoice)
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), counterChoice);

        CardPredicate nonlandPermanent = new CardAllOfPredicate(List.of(
                ExileCardsFromTopUntilEffect.NONLAND, new CardIsPermanentPredicate()));
        CardSubject thatCard = Subjects.cardsThisWay(StepKind.EXILE, nonlandPermanent);
        addTrigger(Triggers.chaosEnsues(), SequenceEffect.of(
                new ExileCardsFromTopUntilEffect(Subjects.you(), nonlandPermanent),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, nonlandPermanent),
                        new MayEffect(ReturnAllCardsExiledWithSourceEffect.putOntoBattlefield(thatCard),
                                "Put that card onto the battlefield? (Otherwise it goes into your hand.)",
                                new PutCardsIntoHandEffect(thatCard)))));
    }

    private static ChooseOneEffect.ChooseOneOption counterMode(String label, CounterType counterType) {
        return new ChooseOneEffect.ChooseOneOption(label,
                new PutCounterOnTargetPermanentEffect(counterType));
    }
}
