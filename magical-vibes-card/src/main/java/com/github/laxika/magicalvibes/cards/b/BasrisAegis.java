package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M21", collectorNumber = "322")
public class BasrisAegis extends Card {

    public BasrisAegis() {
        // Put a +1/+1 counter on each of up to two target creatures.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        // You may search your library and/or graveyard for a card named Basri, Devoted Paladin,
        // reveal it, and put it into your hand. If you search your library this way, shuffle.
        addEffect(EffectSlot.SPELL, new MayEffect(
                new SearchLibraryAndOrGraveyardForNamedCardToHandEffect("Basri, Devoted Paladin"),
                "Search your library and/or graveyard for a card named Basri, Devoted Paladin?"
        ));
    }
}
