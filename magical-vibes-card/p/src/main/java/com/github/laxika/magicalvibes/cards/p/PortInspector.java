package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtDefendingPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MMQ", collectorNumber = "90")
public class PortInspector extends Card {

    public PortInspector() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new MayEffect(new LookAtDefendingPlayerHandEffect(), "Look at defending player's hand?"));
    }
}
