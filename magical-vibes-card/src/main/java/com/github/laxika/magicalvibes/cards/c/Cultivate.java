package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;

@CardRegistration(set = "M11", collectorNumber = "168")
@CardRegistration(set = "M21", collectorNumber = "177")
@CardRegistration(set = "PC2", collectorNumber = "62")
@CardRegistration(set = "A25", collectorNumber = "165")
@CardRegistration(set = "PCA", collectorNumber = "62")
@CardRegistration(set = "STA", collectorNumber = "51")
@CardRegistration(set = "C13", collectorNumber = "139")
@CardRegistration(set = "TMC", collectorNumber = "50")
@CardRegistration(set = "CMD", collectorNumber = "148")
@CardRegistration(set = "SLZ", collectorNumber = "74")
@CardRegistration(set = "SLZ", collectorNumber = "195")
@CardRegistration(set = "SLZ", collectorNumber = "316")
public class Cultivate extends Card {

    public Cultivate() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect());
    }
}
