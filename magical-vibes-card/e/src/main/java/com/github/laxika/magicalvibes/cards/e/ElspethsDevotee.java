package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "THB", collectorNumber = "272")
public class ElspethsDevotee extends Card {

    public ElspethsDevotee() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryAndOrGraveyardForCardToHandEffect(
                        new CardNamedPredicate("Elspeth, Undaunted Hero")),
                "Search your library and/or graveyard for a card named Elspeth, Undaunted Hero?"));
    }
}
