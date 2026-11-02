package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "16")
public class InspiringUnicorn extends Card {

    public InspiringUnicorn() {
        addTrigger(Triggers.attacks(), new BoostAllOwnCreaturesEffect(1, 1));
    }
}
