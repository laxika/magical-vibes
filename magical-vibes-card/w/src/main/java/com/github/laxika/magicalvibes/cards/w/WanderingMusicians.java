package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "250")
public class WanderingMusicians extends Card {

    public WanderingMusicians() {
        addTrigger(Triggers.attacks(), BoostEffect.creaturesYouControl(1, 0));
    }
}
