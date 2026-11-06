package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "81")
public class VisionsOfBrutality extends Card {

    public VisionsOfBrutality() {
        target(TargetFilters.creature())
                .addStaticEffect(new CantBlockEffect())
                .addTrigger(Triggers.enchantedCreatureDealsDamage(),
                        new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
