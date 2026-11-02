package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "201")
@CardRegistration(set = "DDH", collectorNumber = "49")
public class DimirCutpurse extends Card {

    public DimirCutpurse() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false),
                new DrawCardEffect(1)));
    }
}
