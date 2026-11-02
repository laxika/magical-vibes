package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DST", collectorNumber = "128")
public class LichsTomb extends Card {

    public LichsTomb() {
        addStaticEffect(new CantLoseGameFromLifeEffect());
        addTrigger(Triggers.youLoseLife(), new SacrificePermanentsEffect(
                new EventValue(), new PermanentTruePredicate(), SacrificeRecipient.CONTROLLER));
    }
}
