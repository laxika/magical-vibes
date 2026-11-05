package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "109")
public class MoongloveExtractor extends Card {

    public MoongloveExtractor() {
        // Whenever this creature attacks, you draw a card and lose 1 life.
        addTrigger(Triggers.attacks(), new DrawCardEffect(1), new LoseLifeEffect(1));
    }
}
