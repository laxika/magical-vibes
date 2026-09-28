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
@CardRegistration(set = "SLD", collectorNumber = "246")
@CardRegistration(set = "SLD", collectorNumber = "2437")
@CardRegistration(set = "STA", collectorNumber = "51")
@CardRegistration(set = "SOC", collectorNumber = "265")
@CardRegistration(set = "C13", collectorNumber = "139")
@CardRegistration(set = "TMC", collectorNumber = "50")
@CardRegistration(set = "CMD", collectorNumber = "148")
@CardRegistration(set = "SLZ", collectorNumber = "74")
@CardRegistration(set = "SLZ", collectorNumber = "195")
@CardRegistration(set = "SLZ", collectorNumber = "316")
@CardRegistration(set = "MSC", collectorNumber = "172")
@CardRegistration(set = "ECC", collectorNumber = "103")
@CardRegistration(set = "WHO", collectorNumber = "230")
@CardRegistration(set = "PIP", collectorNumber = "196")
@CardRegistration(set = "PIP", collectorNumber = "724")
@CardRegistration(set = "FIC", collectorNumber = "300")
@CardRegistration(set = "MOC", collectorNumber = "295")
@CardRegistration(set = "C21", collectorNumber = "187")
@CardRegistration(set = "40K", collectorNumber = "211")
@CardRegistration(set = "NCC", collectorNumber = "285")
@CardRegistration(set = "DSC", collectorNumber = "174")
@CardRegistration(set = "LTC", collectorNumber = "236")
@CardRegistration(set = "TDC", collectorNumber = "253")
@CardRegistration(set = "AFC", collectorNumber = "155")
@CardRegistration(set = "LCC", collectorNumber = "235")
@CardRegistration(set = "DMC", collectorNumber = "130")
@CardRegistration(set = "C20", collectorNumber = "170")
public class Cultivate extends Card {

    public Cultivate() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect());
    }
}
