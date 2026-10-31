package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayWhileControllingSubtypeEffect;

public class Flameshape extends Card {

    public Flameshape() {
        addSpellEffect(new ExileTopCardMayPlayWhileControllingSubtypeEffect(CardSubtype.WIZARD, 2, true));
    }
}
