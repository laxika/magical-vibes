package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "8")
public class FamishedPaladin extends Card {

    public FamishedPaladin() {
        addStaticEffect(DoesntUntapEffect.self());
        addTrigger(Triggers.youGainLife(),
                new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
