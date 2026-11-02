package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "142")
public class KilnWalker extends Card {

    public KilnWalker() {
        addTrigger(Triggers.attacks(), new BoostSelfEffect(3, 0));
    }
}
