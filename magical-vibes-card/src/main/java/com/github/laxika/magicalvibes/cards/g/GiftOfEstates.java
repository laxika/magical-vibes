package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "9ED", collectorNumber = "15")
public class GiftOfEstates extends Card {

    public GiftOfEstates() {
        // If an opponent controls more lands than you, search your library for up to three
        // Plains cards, reveal them, put them into your hand, then shuffle.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new OpponentControlsMoreLands(),
                new SearchLibraryEffect(new Fixed(3), new CardSubtypePredicate(CardSubtype.PLAINS),
                        LibrarySearchDestination.HAND)));
    }
}
