package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "NEM", collectorNumber = "117")
public class SkyshroudClaim extends Card {

    public SkyshroudClaim() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new Fixed(2),
                new CardSubtypePredicate(CardSubtype.FOREST), LibrarySearchDestination.BATTLEFIELD));
    }
}
