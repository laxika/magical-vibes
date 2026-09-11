package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "SUM", collectorNumber = "105")
@CardRegistration(set = "3ED", collectorNumber = "105")
@CardRegistration(set = "DDC", collectorNumber = "49")
@CardRegistration(set = "DVD", collectorNumber = "49")
public class DemonicTutor extends Card {

    public DemonicTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect());
    }
}
