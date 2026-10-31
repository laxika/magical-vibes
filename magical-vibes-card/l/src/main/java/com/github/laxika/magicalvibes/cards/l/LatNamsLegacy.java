package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleCardsFromHandIntoLibraryThenEffect;

@CardRegistration(set = "ALL", collectorNumber = "30a")
@CardRegistration(set = "ALL", collectorNumber = "30b")
@CardRegistration(set = "CST", collectorNumber = "13")
@CardRegistration(set = "CST", collectorNumber = "30b")
@CardRegistration(set = "ME2", collectorNumber = "52")
public class LatNamsLegacy extends Card {

    public LatNamsLegacy() {
        addSpellEffect(new ShuffleCardsFromHandIntoLibraryThenEffect(
                new RegisterDrawCardsAtNextUpkeepEffect(2)));
    }
}
