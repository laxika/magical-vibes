package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMulticoloredEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "32")
@CardRegistration(set = "EA2", collectorNumber = "8")
public class SoldierOfThePantheon extends Card {

    public SoldierOfThePantheon() {
        addStaticEffect(new ProtectionFromMulticoloredEffect());

        // Whenever an opponent casts a multicolored spell, you gain 1 life.
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardIsMulticoloredPredicate()), new GainLifeEffect(1));
    }
}
