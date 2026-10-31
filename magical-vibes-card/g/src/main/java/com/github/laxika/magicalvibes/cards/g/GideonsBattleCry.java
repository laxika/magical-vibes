package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "WAR", collectorNumber = "267")
public class GideonsBattleCry extends Card {

    public GideonsBattleCry() {
        addSpellEffect(new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
        addSpellEffect(new MayEffect(
                new SearchLibraryAndOrGraveyardForNamedCardToHandEffect("Gideon, the Oathsworn"),
                "Search your library and/or graveyard for a card named Gideon, the Oathsworn?"));
    }
}
