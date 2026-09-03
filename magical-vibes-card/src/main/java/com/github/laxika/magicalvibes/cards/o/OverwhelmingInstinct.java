package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ONS", collectorNumber = "276")
public class OverwhelmingInstinct extends Card {

    public OverwhelmingInstinct() {
        // Whenever you attack with three or more creatures, draw a card.
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new MinimumAttackers(3), new DrawCardEffect()));
    }
}
