package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

public class VentureDeeper extends Card {

    public VentureDeeper() {
        addSpellEffect(new MillEffect(4, MillRecipient.TARGET_PLAYER));
    }
}
