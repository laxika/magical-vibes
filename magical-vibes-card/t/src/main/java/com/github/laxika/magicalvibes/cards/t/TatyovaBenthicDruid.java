package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "206")
@CardRegistration(set = "FDN", collectorNumber = "247")
@CardRegistration(set = "DMR", collectorNumber = "203")
@CardRegistration(set = "CMM", collectorNumber = "358")
@CardRegistration(set = "CMM", collectorNumber = "687")
@CardRegistration(set = "DSC", collectorNumber = "235")
@CardRegistration(set = "M3C", collectorNumber = "273")
@CardRegistration(set = "LCC", collectorNumber = "290")
public class TatyovaBenthicDruid extends Card {

    public TatyovaBenthicDruid() {
        // Landfall — Whenever a land you control enters, you gain 1 life and draw a card.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new GainLifeEffect(1),
                new DrawCardEffect());
    }
}
