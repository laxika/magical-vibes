package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "135")
@CardRegistration(set = "SCG", collectorNumber = "87")
@CardRegistration(set = "GN3", collectorNumber = "73")
@CardRegistration(set = "C15", collectorNumber = "151")
@CardRegistration(set = "C16", collectorNumber = "124")
@CardRegistration(set = "SCD", collectorNumber = "132")
public class DragonMage extends Card {

    public DragonMage() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new DiscardHandEffect(DiscardRecipient.EACH_PLAYER),
                new EachPlayerDrawsCardEffect(7)));
    }
}
