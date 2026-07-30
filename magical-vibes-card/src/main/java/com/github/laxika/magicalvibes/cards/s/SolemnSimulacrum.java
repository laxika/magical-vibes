package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "M12", collectorNumber = "217")
public class SolemnSimulacrum extends Card {

    public SolemnSimulacrum() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        "Search your library for a basic land card?"));
        addEffect(EffectSlot.ON_DEATH, new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
