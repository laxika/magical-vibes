package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

public class VandalsEdit extends Card {

    public VandalsEdit() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new LoseLifeEffect(2, Subjects.eachPlayer()));
    }
}
