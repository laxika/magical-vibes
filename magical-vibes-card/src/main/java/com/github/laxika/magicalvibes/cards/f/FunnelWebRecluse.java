package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MH2", collectorNumber = "161")
public class FunnelWebRecluse extends Card {

    public FunnelWebRecluse() {
        // Morbid — When Funnel-Web Recluse enters the battlefield, if a creature died this turn,
        // investigate.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Morbid(), CreateTokenEffect.ofClueToken(1)));
    }
}
