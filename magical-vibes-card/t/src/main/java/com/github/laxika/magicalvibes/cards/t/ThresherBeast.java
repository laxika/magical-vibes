package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "128")
public class ThresherBeast extends Card {

    public ThresherBeast() {
        addTrigger(Triggers.becomesBlocked(),
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.DEFENDING_PLAYER));
    }
}
