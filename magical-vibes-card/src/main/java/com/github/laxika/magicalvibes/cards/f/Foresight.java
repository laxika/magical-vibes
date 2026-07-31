package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "ALL", collectorNumber = "29a")
@CardRegistration(set = "ALL", collectorNumber = "29b")
public class Foresight extends Card {

    public Foresight() {
        // "Search your library for three cards, exile them, then shuffle."
        addEffect(EffectSlot.SPELL,
                new SearchLibraryEffect(new Fixed(3), null, LibrarySearchDestination.EXILE));
        // "Draw a card at the beginning of the next turn's upkeep."
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
