package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "HOB", collectorNumber = "97")
public class GandalfSparkStarter extends Card {

    public GandalfSparkStarter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                DealDividedDamageEffect.chosenAmongAnyTargetsEtb(3, 3));
    }
}
