package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DamageToControllerCounterReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;

@CardRegistration(set = "SCG", collectorNumber = "14")
public class ForceBubble extends Card {

    public ForceBubble() {
        addStaticEffect(new DamageToControllerCounterReplacementEffect(CounterType.DEPLETION));
        addTrigger(Triggers.sourceState(new PermanentHasAtLeastCountersPredicate(CounterType.DEPLETION, 4)),
                new SacrificeSelfEffect());
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                RemoveCountersEffect.allFromSelf(CounterType.DEPLETION));
    }
}
