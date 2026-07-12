package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "104")
@CardRegistration(set = "8ED", collectorNumber = "98")
@CardRegistration(set = "M10", collectorNumber = "69")
public class SageOwl extends Card {

    public SageOwl() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReorderTopCardsOfLibraryEffect(4));
    }
}
