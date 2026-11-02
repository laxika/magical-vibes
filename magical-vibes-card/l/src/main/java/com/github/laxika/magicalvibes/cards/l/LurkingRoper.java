package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "194")
@CardRegistration(set = "HBG", collectorNumber = "216")
public class LurkingRoper extends Card {

    public LurkingRoper() {
        addStaticEffect(DoesntUntapEffect.self());
        addTrigger(Triggers.youGainLife(),
                new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
