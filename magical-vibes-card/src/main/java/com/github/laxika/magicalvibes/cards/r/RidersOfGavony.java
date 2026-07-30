package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromChosenTypeToOwnCreaturesEffect;

@CardRegistration(set = "AVR", collectorNumber = "33")
public class RidersOfGavony extends Card {

    public RidersOfGavony() {
        // As this creature enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // Human creatures you control have protection from creatures of the chosen type.
        addEffect(EffectSlot.STATIC,
                new GrantProtectionFromChosenTypeToOwnCreaturesEffect(CardSubtype.HUMAN));
    }
}
