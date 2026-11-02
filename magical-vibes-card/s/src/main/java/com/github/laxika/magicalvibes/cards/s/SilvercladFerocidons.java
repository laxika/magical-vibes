package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "115")
@CardRegistration(set = "PZA", collectorNumber = "9")
public class SilvercladFerocidons extends Card {

    public SilvercladFerocidons() {
        // Enrage — Whenever this creature is dealt damage, each opponent sacrifices a permanent
        // of their choice.
        addTrigger(Triggers.dealtDamage(),
                new SacrificePermanentsEffect(1, new PermanentTruePredicate(), SacrificeRecipient.EACH_OPPONENT));
    }
}
