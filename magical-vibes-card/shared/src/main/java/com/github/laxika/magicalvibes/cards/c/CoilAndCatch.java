package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;

public class CoilAndCatch extends Card {

    public CoilAndCatch() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addSpellEffect(new ShuffleIntoLibraryEffect());
    }
}
