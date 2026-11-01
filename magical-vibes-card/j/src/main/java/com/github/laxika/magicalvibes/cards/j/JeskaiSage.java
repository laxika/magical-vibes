package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "38")
public class JeskaiSage extends Card {

    public JeskaiSage() {
        // Prowess is a Scryfall-loaded keyword, so its boost is handled by the engine.
        // When this creature dies, draw a card.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect());
    }
}
