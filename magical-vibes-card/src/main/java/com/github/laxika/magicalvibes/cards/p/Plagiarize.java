package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlagiarizeEffect;

public class Plagiarize extends Card {

    public Plagiarize() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new PlagiarizeEffect());
    }
}
