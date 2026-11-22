package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "89")
@CardRegistration(set = "MMA", collectorNumber = "108")
public class CountrysideCrusher extends Card {

    public CountrysideCrusher() {
        var landCard = new CardTypePredicate(CardType.LAND);
        // At the beginning of your upkeep, reveal the top card of your library. If it's a land card,
        // put it into your graveyard and repeat this process.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new RevealCardsFromTopUntilEffect(new CardNotPredicate(landCard)),
                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay(landCard)));

        // Whenever a land card is put into your graveyard from anywhere, put a +1/+1 counter on this creature.
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.YOU_OWN),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
