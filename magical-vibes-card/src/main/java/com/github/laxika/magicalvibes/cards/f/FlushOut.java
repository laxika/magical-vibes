package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;

public class FlushOut extends Card {

    public FlushOut() {
        addEffect(EffectSlot.SPELL, new DiscardAndDrawCardEffect(1, 2));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
