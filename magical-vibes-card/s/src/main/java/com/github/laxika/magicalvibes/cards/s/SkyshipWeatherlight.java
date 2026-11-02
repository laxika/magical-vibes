package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutRandomCardExiledWithSourceIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "133")
public class SkyshipWeatherlight extends Card {

    public SkyshipWeatherlight() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryForCardsToExileWithSourceEffect(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE)))));
        addActivatedAbility(new ActivatedAbility(true, "{4}",
                List.of(new PutRandomCardExiledWithSourceIntoOwnersHandEffect()),
                "{4}, {T}: Choose a card at random that was exiled with Skyship Weatherlight. Put that card into its owner's hand."));
    }
}
