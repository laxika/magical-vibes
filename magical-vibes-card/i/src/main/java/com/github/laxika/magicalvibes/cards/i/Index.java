package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "84")
@CardRegistration(set = "M13", collectorNumber = "55")
@CardRegistration(set = "APC", collectorNumber = "25")
public class Index extends Card {

    public Index() {
        addEffect(EffectSlot.SPELL, new ReorderTopCardsOfLibraryEffect(5));
    }
}
