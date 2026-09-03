package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;

@CardRegistration(set = "ONS", collectorNumber = "53")
public class SharedTriumph extends Card {

    public SharedTriumph() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenSubtypeEffect(1, 1, null, true));
    }
}
