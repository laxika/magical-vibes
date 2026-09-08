package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "SUM", collectorNumber = "105")
public class DemonicTutor extends Card {

    public DemonicTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect());
    }
}
