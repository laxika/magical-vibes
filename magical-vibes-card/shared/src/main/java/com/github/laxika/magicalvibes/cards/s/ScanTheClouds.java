package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

public class ScanTheClouds extends Card {

    public ScanTheClouds() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(2, Subjects.you()));
    }
}
