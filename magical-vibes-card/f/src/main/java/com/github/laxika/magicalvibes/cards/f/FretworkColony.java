package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "83")
@CardRegistration(set = "KLR", collectorNumber = "90")
public class FretworkColony extends Card {

    public FretworkColony() {
        addStaticEffect(new CantBlockEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                new LoseLifeEffect(1)));
    }
}
