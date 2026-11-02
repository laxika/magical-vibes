package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "145")
public class RuinousMinotaur extends Card {

    public RuinousMinotaur() {
        addTrigger(Triggers.dealsDamageToOpponent(),
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER));
    }
}
