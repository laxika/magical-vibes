package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;

public class CruelEdict extends Card {

    public CruelEdict() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new SacrificeCreatureEffect());
    }
}
