package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "CON", collectorNumber = "91")
public class ShardConvergence extends Card {

    public ShardConvergence() {
        // "Search your library for a Plains card, an Island card, a Swamp card, and a Mountain card.
        // Reveal those cards, put them into your hand, then shuffle." Each restricted subtype search
        // reveals its pick and may fail to find; a card found for one subtype leaves the library
        // before the next search, so a dual-type card can only satisfy one subtype (CR-consistent).
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.PLAINS)));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.ISLAND)));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.SWAMP)));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.MOUNTAIN)));
    }
}
