package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "SHM", collectorNumber = "57")
public class BeseechTheQueen extends Card {

    public BeseechTheQueen() {
        // Search your library for a card with mana value <= the number of lands you control,
        // reveal it, put it into your hand, then shuffle. Any-card tutor (null filter) restricted
        // by a dynamic mana-value bound driven by the controller's land count.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(null, LibrarySearchDestination.HAND,
                new ManaValueBound(new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER), false, 0)));
    }
}
