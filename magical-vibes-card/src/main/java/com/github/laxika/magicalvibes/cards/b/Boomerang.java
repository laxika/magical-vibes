package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;

public class Boomerang extends Card {

    public Boomerang() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandEffect());
    }
}
