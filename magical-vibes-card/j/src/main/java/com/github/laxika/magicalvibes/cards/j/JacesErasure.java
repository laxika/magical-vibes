package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "59")
@CardRegistration(set = "M12", collectorNumber = "60")
public class JacesErasure extends Card {

    public JacesErasure() {
        addTrigger(Triggers.youDrawCard(), new MayEffect(
                new MillEffect(1, MillRecipient.TARGET_PLAYER),
                "Have target player mill a card?"
        ));
    }
}
