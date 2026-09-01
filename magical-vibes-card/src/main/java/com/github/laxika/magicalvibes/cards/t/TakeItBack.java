package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellToHandEffect;

public class TakeItBack extends Card {

    public TakeItBack() {
        addEffect(EffectSlot.SPELL, new ReturnTargetSpellToHandEffect());
    }
}
