package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

public class Cancel extends Card {

    public Cancel() {
        setNeedsSpellTarget(true);
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
