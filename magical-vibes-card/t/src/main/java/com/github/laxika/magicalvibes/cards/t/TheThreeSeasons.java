package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "231")
public class TheThreeSeasons extends Card {

    public TheThreeSeasons() {
        addTrigger(Triggers.chapter(1), new MillEffect(3, Subjects.you()));
        addTrigger(Triggers.chapter(2), new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardAllOfPredicate(java.util.List.of(
                        new CardIsPermanentPredicate(),
                        new CardSupertypePredicate(CardSupertype.SNOW))), 2));
        addTrigger(Triggers.chapter(3),
                ChooseCardEffect.exactly(Subjects.cardsInGraveyards(Subjects.eachPlayer(), null), 3,
                        "three cards in each graveyard").fromEachOwner(),
                PutCardsIntoLibraryEffect.shuffledIn(Subjects.cardsThisWay(StepKind.CHOOSE)));
    }
}
