package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.OpponentPlayerTargetFilter;

@CardRegistration(set = "10E", collectorNumber = "178")
public class SleeperAgent extends Card {

    public SleeperAgent() {
        setNeedsTarget(true);
        setTargetFilter(new OpponentPlayerTargetFilter());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TargetPlayerGainsControlOfSourceCreatureEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DealDamageToControllerEffect(2));
    }
}
