package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsOfChosenColorEffect;

@CardRegistration(set = "ZEN", collectorNumber = "13")
@CardRegistration(set = "MM2", collectorNumber = "20")
@CardRegistration(set = "V15", collectorNumber = "9")
@CardRegistration(set = "MB2", collectorNumber = "12")
@CardRegistration(set = "AA4", collectorNumber = "3")
public class IonaShieldOfEmeria extends Card {

    public IonaShieldOfEmeria() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addStaticEffect(new OpponentsCantCastSpellsOfChosenColorEffect());
    }
}
