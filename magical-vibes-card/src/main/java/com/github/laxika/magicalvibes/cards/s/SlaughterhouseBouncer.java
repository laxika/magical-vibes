package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "54")
public class SlaughterhouseBouncer extends Card {

    public SlaughterhouseBouncer() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_DEATH,
                new ConditionalEffect(new ControllerHandEmpty(), new BoostTargetCreatureEffect(-3, -3)));
    }
}
