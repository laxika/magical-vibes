package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "240")
public class SafewrightQuest extends Card {

    public SafewrightQuest() {
        // Search your library for a Forest or Plains card, reveal it, put it into your hand,
        // then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.FOREST),
                new CardSubtypePredicate(CardSubtype.PLAINS)))));
    }
}
