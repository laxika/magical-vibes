package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.TotalRadCountersAmongPlayers;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEachPlayerRadCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "51")
@CardRegistration(set = "PIP", collectorNumber = "579")
public class Vault12TheNecropolis extends Card {

    public Vault12TheNecropolis() {
        addTrigger(Triggers.chapter(1), new GiveEachPlayerRadCounterEffect(3));

        addTrigger(Triggers.chapter(2), new CreateTokenEffect(
                new TotalRadCountersAmongPlayers(), "Zombie Mutant", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE, CardSubtype.MUTANT), Set.of(), Set.of()));

        addTrigger(Triggers.chapter(3), new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 2,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ZOMBIE, CardSubtype.MUTANT))))));
    }
}
