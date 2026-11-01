package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "107")
@CardRegistration(set = "DDQ", collectorNumber = "60")
@CardRegistration(set = "TSR", collectorNumber = "325")
@CardRegistration(set = "E01", collectorNumber = "34")
public class HarvesterOfSouls extends Card {

    public HarvesterOfSouls() {
        // Whenever another nontoken creature dies, you may draw a card. Deathtouch comes from Scryfall.
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANOTHER),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
