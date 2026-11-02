package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "70")
@CardRegistration(set = "WOT", collectorNumber = "26")
public class SpreadingSeas extends Card {

    public SpreadingSeas() {
        target(TargetFilters.land())
                .addStaticEffect(new EnchantedPermanentBecomesTypeEffect(CardSubtype.ISLAND));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));
    }
}
