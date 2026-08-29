package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "USG", collectorNumber = "254")
public class GaeasBounty extends Card {

    public GaeasBounty() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new Fixed(2),
                new CardSubtypePredicate(CardSubtype.FOREST), LibrarySearchDestination.HAND));
    }
}
