package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "27")
public class CephalidAristocrat extends Card {

    public CephalidAristocrat() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                new MillEffect(2, MillRecipient.CONTROLLER));
    }
}
