package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;

public class FlushOut extends Card {

    public FlushOut() {
        addSpellEffect(new DiscardAndDrawCardEffect(1, 2));
        addSpellEffect(new ShuffleIntoLibraryEffect());
    }
}
