package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureOrPayManaCost;

@CardRegistration(set = "STX", collectorNumber = "121")
public class BayouGroff extends Card {

    public BayouGroff() {
        // As an additional cost to cast this spell, sacrifice a creature or pay {3}.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureOrPayManaCost("{3}"));
    }
}
