package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "SOI", collectorNumber = "101")
@CardRegistration(set = "PIO", collectorNumber = "84")
public class BeholdTheBeyond extends Card {

    public BeholdTheBeyond() {
        addSpellEffect(new DiscardHandEffect());
        addSpellEffect(new SearchLibraryEffect(null, 3, 3));
    }
}
