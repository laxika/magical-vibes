package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IMA", collectorNumber = "58")
@CardRegistration(set = "GN3", collectorNumber = "29")
@CardRegistration(set = "C15", collectorNumber = "12")
@CardRegistration(set = "C20", collectorNumber = "113")
@CardRegistration(set = "BLC", collectorNumber = "167")
public class IllusoryAmbusher extends Card {

    public IllusoryAmbusher() {
        addTrigger(Triggers.dealtDamage(), new DrawCardEffect(new EventValue()));
    }
}
