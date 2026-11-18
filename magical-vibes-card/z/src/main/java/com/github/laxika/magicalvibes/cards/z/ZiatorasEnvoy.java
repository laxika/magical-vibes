package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPlayLandOrCastFreeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "232")
public class ZiatorasEnvoy extends Card {

    public ZiatorasEnvoy() {
        addCastingOption(AlternateHandCast.blitz("{2}{B}{R}{G}"));

        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new LookAtTopCardMayPlayLandOrCastFreeEffect(new EventValue()));
    }
}
