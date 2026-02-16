package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;

public class Discombobulate extends Card {

    public Discombobulate() {
        setNeedsSpellTarget(true);
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new ReorderTopCardsOfLibraryEffect(4));
    }
}
