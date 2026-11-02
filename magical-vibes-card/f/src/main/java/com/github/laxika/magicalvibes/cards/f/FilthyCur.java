package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "136")
public class FilthyCur extends Card {

    public FilthyCur() {
        // Whenever this creature is dealt damage, you lose that much life.
        addTrigger(Triggers.dealtDamage(),
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER));
    }
}
