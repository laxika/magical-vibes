package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "152")
@CardRegistration(set = "TSR", collectorNumber = "213")
public class KeenSense extends Card {

    public KeenSense() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.dealsDamageToOpponent(),
                        new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
