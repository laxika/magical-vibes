package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "4")
public class Contemplation extends Card {

    public Contemplation() {
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(null, List.of(new GainLifeEffect(1))));
    }
}
