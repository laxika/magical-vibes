package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

public class CantWakeUp extends Card {

    public CantWakeUp() {
        addSpellEffect(new MillEffect(4, Subjects.targetPlayer()));
    }
}
