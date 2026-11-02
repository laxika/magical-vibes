package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMC", collectorNumber = "14")
@CardRegistration(set = "DMC", collectorNumber = "90")
public class TwoHeadedHellkite extends Card {

    public TwoHeadedHellkite() {
        addTrigger(Triggers.attacks(), new DrawCardEffect(2));
    }
}
