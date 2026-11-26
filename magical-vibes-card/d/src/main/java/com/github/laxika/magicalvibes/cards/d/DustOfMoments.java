package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsSuspendedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "5")
public class DustOfMoments extends Card {

    public DustOfMoments() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Remove two time counters from each permanent and each suspended card",
                        List.of(
                                new RemoveCountersEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                                        new PermanentHasCountersPredicate(CounterType.TIME)), CounterType.TIME, 2),
                                new RemoveCountersFromExiledCardsEffect(
                                        Subjects.exiledCards(new CardIsSuspendedPredicate()), CounterType.TIME, 2))),
                new ChooseOneEffect.ChooseOneOption(
                        "Put two time counters on each permanent with a time counter on it and each suspended card",
                        List.of(
                                PutCountersEffect.onEach(CounterType.TIME, 2, new PermanentHasCountersPredicate(CounterType.TIME)),
                                new PutCounterOnExiledCardsEffect(
                                        Subjects.exiledCards(new CardIsSuspendedPredicate()), CounterType.TIME, 2)))
        )));
    }
}
