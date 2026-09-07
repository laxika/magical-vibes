package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;

@CardRegistration(set = "LCI", collectorNumber = "249")
public class ChimilTheInnerSun extends Card {

    public ChimilTheInnerSun() {
        addEffect(EffectSlot.STATIC, new ControllerSpellsCantBeCounteredEffect());
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new DiscoverEffect(5));
    }
}
