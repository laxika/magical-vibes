package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "100")
public class FungalShambler extends Card {

    public FungalShambler() {
        addTrigger(Triggers.dealsDamageToOpponent(), SequenceEffect.of(
                new DrawCardEffect(1),
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false)));
    }
}
