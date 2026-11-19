package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class OdiousWitch extends Card {

    public OdiousWitch() {
        addTrigger(Triggers.attacks(),
                new LoseLifeEffect(1, Subjects.defendingPlayer()),
                new GainLifeEffect(1));
    }
}
