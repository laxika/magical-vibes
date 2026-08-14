package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachLibraryAndMayCastSpellsEffect;

@CardRegistration(set = "FDN", collectorNumber = "194")
public class EtaliPrimalStorm extends Card {

    public EtaliPrimalStorm() {
        addEffect(EffectSlot.ON_ATTACK, new ExileTopCardOfEachLibraryAndMayCastSpellsEffect());
    }
}
