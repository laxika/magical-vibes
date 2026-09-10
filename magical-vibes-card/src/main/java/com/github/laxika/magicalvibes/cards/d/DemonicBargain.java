package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "VOW", collectorNumber = "103")
public class DemonicBargain extends Card {

    public DemonicBargain() {
        for (int i = 0; i < 13; i++) {
            addEffect(EffectSlot.SPELL, new ExileTopCardOfOwnLibraryEffect(false));
        }
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect());
    }
}
