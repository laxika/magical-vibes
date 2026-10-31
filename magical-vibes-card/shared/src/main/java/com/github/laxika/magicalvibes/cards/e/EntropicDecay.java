package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

public class EntropicDecay extends Card {

    public EntropicDecay() {
        addSpellEffect(new MillEffect(4, MillRecipient.CONTROLLER));
    }
}
