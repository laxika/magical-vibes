package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "217")
@CardRegistration(set = "VMA", collectorNumber = "178")
@CardRegistration(set = "DMR", collectorNumber = "129")
@CardRegistration(set = "C20", collectorNumber = "155")
public class LightningRift extends Card {

    public LightningRift() {
        addTrigger(Triggers.playerCyclesCard(), UnlessPaysEffect.youMayPay("{1}", DealDamageEffect.toAnyTarget(2))
                .withPrompt("Pay {1} to have Lightning Rift deal 2 damage to any target?"));
    }
}
