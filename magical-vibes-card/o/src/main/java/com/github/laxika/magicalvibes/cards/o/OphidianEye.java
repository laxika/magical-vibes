package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "70")
public class OphidianEye extends Card {

    public OphidianEye() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.dealsDamageToOpponent(), new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
