package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "TMP", collectorNumber = "198")
@CardRegistration(set = "TPR", collectorNumber = "152")
@CardRegistration(set = "BRB", collectorNumber = "60")
public class RollingThunder extends Card {

    public RollingThunder() {
        // Rolling Thunder deals X damage divided as you choose among any number of targets.
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(new XValue()));
    }
}
