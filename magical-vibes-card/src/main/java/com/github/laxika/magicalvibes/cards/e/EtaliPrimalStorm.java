package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachLibraryAndMayCastSpellsEffect;

@CardRegistration(set = "FDN", collectorNumber = "194")
@CardRegistration(set = "FDN", collectorNumber = "329")
@CardRegistration(set = "FDN", collectorNumber = "391")
@CardRegistration(set = "LCC", collectorNumber = "224")
@CardRegistration(set = "C21", collectorNumber = "167")
@CardRegistration(set = "C20", collectorNumber = "151")
@CardRegistration(set = "NCC", collectorNumber = "268")
@CardRegistration(set = "RIX", collectorNumber = "100")
@CardRegistration(set = "SLD", collectorNumber = "1123")
@CardRegistration(set = "SLD", collectorNumber = "1389")
@CardRegistration(set = "TSR", collectorNumber = "342")
@CardRegistration(set = "MKC", collectorNumber = "152")
@CardRegistration(set = "AFC", collectorNumber = "126")
@CardRegistration(set = "DMC", collectorNumber = "121")
public class EtaliPrimalStorm extends Card {

    public EtaliPrimalStorm() {
        addEffect(EffectSlot.ON_ATTACK, new ExileTopCardOfEachLibraryAndMayCastSpellsEffect());
    }
}
