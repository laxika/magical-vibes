package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WWK", collectorNumber = "130")
@CardRegistration(set = "DDP", collectorNumber = "29")
@CardRegistration(set = "C13", collectorNumber = "256")
@CardRegistration(set = "C14", collectorNumber = "267")
@CardRegistration(set = "C15", collectorNumber = "264")
@CardRegistration(set = "M3C", collectorNumber = "304")
@CardRegistration(set = "C18", collectorNumber = "221")
@CardRegistration(set = "CMA", collectorNumber = "227")
@CardRegistration(set = "CM2", collectorNumber = "214")
@CardRegistration(set = "ZNC", collectorNumber = "119")
public class SeersSundial extends Card {

    public SeersSundial() {
        // Landfall — Whenever a land you control enters, you may pay {2}. If you do, draw a card.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new MayPayManaEffect("{2}", new DrawCardEffect(), "Pay {2} to draw a card?"));
    }
}
