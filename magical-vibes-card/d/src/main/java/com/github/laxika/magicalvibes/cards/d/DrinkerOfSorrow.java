package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "66")
public class DrinkerOfSorrow extends Card {

    public DrinkerOfSorrow() {
        addStaticEffect(new CantBlockEffect());
        addTrigger(Triggers.dealsCombatDamage(),
                new SacrificePermanentsEffect(1, new PermanentTruePredicate(), SacrificeRecipient.CONTROLLER));
    }
}
