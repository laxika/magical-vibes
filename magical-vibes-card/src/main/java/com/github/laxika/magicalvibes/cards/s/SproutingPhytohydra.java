package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "DIS", collectorNumber = "95")
public class SproutingPhytohydra extends Card {

    public SproutingPhytohydra() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new MayEffect(
                new CreateTokenCopyOfSourceEffect(),
                "Create a token that's a copy of Sprouting Phytohydra?"));
    }
}
