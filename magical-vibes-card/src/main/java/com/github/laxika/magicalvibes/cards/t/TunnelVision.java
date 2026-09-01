package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealUntilNamedPutOnTopRestToGraveyardEffect;

@CardRegistration(set = "RAV", collectorNumber = "72")
public class TunnelVision extends Card {

    public TunnelVision() {
        addEffect(EffectSlot.SPELL, new ChooseNameRevealUntilNamedPutOnTopRestToGraveyardEffect());
    }
}
