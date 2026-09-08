package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

/** Demonic Tutor, the prepare spell of Emeritus of Woe. */
public class DemonicTutor extends Card {

    public DemonicTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect());
    }
}
