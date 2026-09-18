package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForEachChosenColorEffect;

@CardRegistration(set = "RVR", collectorNumber = "266")
@CardRegistration(set = "C15", collectorNumber = "54")
public class SealOfTheGuildpact extends Card {

    public SealOfTheGuildpact() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect(2));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForEachChosenColorEffect());
    }
}
