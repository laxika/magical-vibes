package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "128")
public class ViciousConquistador extends Card {

    public ViciousConquistador() {
        addTrigger(Triggers.attacks(), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
