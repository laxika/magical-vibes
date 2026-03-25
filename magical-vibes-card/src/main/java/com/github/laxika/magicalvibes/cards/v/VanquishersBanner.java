package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenSubtypeSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "251")
public class VanquishersBanner extends Card {

    public VanquishersBanner() {
        // As Vanquisher's Banner enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // Creatures you control of the chosen type get +1/+1.
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenSubtypeEffect(1, 1));

        // Whenever you cast a creature spell of the chosen type, draw a card.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new ChosenSubtypeSpellCastTriggerEffect(
                List.of(new DrawCardEffect())
        ));
    }
}
