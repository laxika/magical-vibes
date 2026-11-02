package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "19")
public class SilkenfistFighter extends Card {

    public SilkenfistFighter() {
        addTrigger(Triggers.becomesBlocked(), new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
