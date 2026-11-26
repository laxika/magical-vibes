package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "47")
@CardRegistration(set = "TPR", collectorNumber = "33")
@CardRegistration(set = "VMA", collectorNumber = "47")
public class SoltariTrooper extends Card {

    public SoltariTrooper() {
        addTrigger(Triggers.attacks(), BoostEffect.self(1, 1));
    }
}
