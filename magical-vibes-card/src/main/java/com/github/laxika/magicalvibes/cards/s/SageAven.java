package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "95")
@CardRegistration(set = "ONS", collectorNumber = "111")
public class SageAven extends Card {

    public SageAven() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReorderTopCardsOfLibraryEffect(4));
    }
}
