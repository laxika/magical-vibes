package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleCardsFromHandIntoLibraryThenEffect;

@CardRegistration(set = "ALL", collectorNumber = "30a")
@CardRegistration(set = "ALL", collectorNumber = "30b")
public class LatNamsLegacy extends Card {

    public LatNamsLegacy() {
        addEffect(EffectSlot.SPELL, new ShuffleCardsFromHandIntoLibraryThenEffect(
                new RegisterDrawCardsAtNextUpkeepEffect(2)));
    }
}
