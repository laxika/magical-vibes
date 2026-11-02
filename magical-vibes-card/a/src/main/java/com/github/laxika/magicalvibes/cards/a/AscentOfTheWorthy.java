package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageToChosenCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "202")
public class AscentOfTheWorthy extends Card {

    public AscentOfTheWorthy() {
        addTrigger(Triggers.chapter(1), new RedirectAllDamageToChosenCreatureUntilNextTurnEffect());

        addTrigger(Triggers.chapter(2), new RedirectAllDamageToChosenCreatureUntilNextTurnEffect());

        addTrigger(Triggers.chapter(3), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .enterWithCounter(CounterType.FLYING)
                .enterWithCounterCount(1)
                .grantSubtypes(List.of(CardSubtype.ANGEL, CardSubtype.WARRIOR))
                .build());
    }
}
