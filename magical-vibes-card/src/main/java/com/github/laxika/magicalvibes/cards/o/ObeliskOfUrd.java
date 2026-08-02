package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;

@CardRegistration(set = "M15", collectorNumber = "222")
public class ObeliskOfUrd extends Card {

    public ObeliskOfUrd() {
        // As this artifact enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // Creatures you control of the chosen type get +2/+2.
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenSubtypeEffect(2, 2));
    }
}
