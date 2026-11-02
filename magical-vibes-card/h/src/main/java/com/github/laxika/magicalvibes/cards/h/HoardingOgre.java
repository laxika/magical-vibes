package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "146")
public class HoardingOgre extends Card {

    public HoardingOgre() {
        addTrigger(Triggers.attacks(), new RollD20Effect(
                CreateTokenEffect.ofTreasureToken(1),
                CreateTokenEffect.ofTreasureToken(2),
                CreateTokenEffect.ofTreasureToken(3)
        ));
    }
}
