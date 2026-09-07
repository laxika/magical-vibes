package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestToughnessAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MKM", collectorNumber = "162")
public class GlintWeaver extends Card {

    public GlintWeaver() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                DistributeCountersAmongTargetsEffect.chosenAmongTargetCreaturesEtb(
                        CounterType.PLUS_ONE_PLUS_ONE, 3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GainLifeEffect(new GreatestToughnessAmongControlled()));
    }
}
