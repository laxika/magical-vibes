package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "262")
public class EmberwildeCaliph extends Card {

    public EmberwildeCaliph() {
        // This creature attacks each combat if able.
        addStaticEffect(new MustAttackEffect());

        // Whenever this creature deals damage, you lose that much life.
        addTrigger(Triggers.dealsDamage(), new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER, false));
    }
}
