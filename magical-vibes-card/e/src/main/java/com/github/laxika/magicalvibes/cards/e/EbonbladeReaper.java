package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.HalfControllerLifeRoundedUp;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "141")
public class EbonbladeReaper extends Card {

    public EbonbladeReaper() {
        addMorph("{3}{B}{B}");
        addTrigger(Triggers.attacks(),
                new LoseLifeEffect(new HalfControllerLifeRoundedUp(), LoseLifeRecipient.CONTROLLER));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new LoseLifeEffect(new HalvedRoundedUp(new TargetPlayerLifeTotal()),
                        LoseLifeRecipient.TARGET_PLAYER));
    }
}
