package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "INR", collectorNumber = "94")
public class WretchedThrong extends Card {

    public WretchedThrong() {
        // When this creature dies, you may search your library for a card named Wretched Throng,
        // reveal it, put it into your hand, then shuffle.
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new SearchLibraryEffect(new CardNamedPredicate("Wretched Throng")),
                "Search your library for a card named Wretched Throng?"
        ));
    }
}
