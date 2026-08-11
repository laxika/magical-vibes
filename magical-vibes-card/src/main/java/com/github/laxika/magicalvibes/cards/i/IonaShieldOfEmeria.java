package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsOfChosenColorEffect;

@CardRegistration(set = "ZEN", collectorNumber = "13")
public class IonaShieldOfEmeria extends Card {

    public IonaShieldOfEmeria() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.STATIC, new OpponentsCantCastSpellsOfChosenColorEffect());
    }
}
