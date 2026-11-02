package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "95")
public class SproutingPhytohydra extends Card {

    public SproutingPhytohydra() {
        addTrigger(Triggers.dealtDamage(), new MayEffect(
                new CreateTokenCopyOfSourceEffect(),
                "Create a token that's a copy of Sprouting Phytohydra?"));
    }
}
