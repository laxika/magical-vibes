package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleCardsFromHandIntoLibraryThenEffect;

@CardRegistration(set = "ROE", collectorNumber = "86")
@CardRegistration(set = "PC2", collectorNumber = "25")
@CardRegistration(set = "PCA", collectorNumber = "25")
public class SeeBeyond extends Card {

    public SeeBeyond() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new ShuffleCardsFromHandIntoLibraryThenEffect(null));
    }
}
