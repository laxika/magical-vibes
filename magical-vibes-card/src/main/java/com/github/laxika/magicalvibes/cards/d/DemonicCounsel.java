package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "DSK", collectorNumber = "92")
public class DemonicCounsel extends Card {

    public DemonicCounsel() {
        // Search your library for a Demon card, reveal it, put it into your hand, then shuffle.
        // Delirium — If there are four or more card types among cards in your graveyard, instead
        // search your library for any card, put it into your hand, then shuffle.
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Delirium(),
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.DEMON)),
                new SearchLibraryEffect()
        ));
    }
}
