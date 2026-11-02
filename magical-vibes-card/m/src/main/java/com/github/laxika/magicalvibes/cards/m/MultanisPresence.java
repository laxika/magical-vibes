package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "109")
public class MultanisPresence extends Card {

    public MultanisPresence() {
        addTrigger(Triggers.yourSpellCountered(), new DrawCardEffect());
    }
}
