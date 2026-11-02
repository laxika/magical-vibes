package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class HakkaWhisperingRaven extends Card {

    public HakkaWhisperingRaven() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(ReturnToHandEffect.self(), new ScryEffect(2)));
    }
}
