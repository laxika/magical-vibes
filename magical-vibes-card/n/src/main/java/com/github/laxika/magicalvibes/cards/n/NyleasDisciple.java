package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "167")
@CardRegistration(set = "PIO", collectorNumber = "301")
public class NyleasDisciple extends Card {

    public NyleasDisciple() {
        // When this creature enters, you gain life equal to your devotion to green.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GainLifeEffect(new ColorManaSymbolsAmongControlledPermanents(ManaColor.GREEN)));
    }
}
