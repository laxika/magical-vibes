package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "152")
public class RavenousSkirge extends Card {

    public RavenousSkirge() {
        addTrigger(Triggers.attacks(), BoostEffect.self(2, 0));
    }
}
