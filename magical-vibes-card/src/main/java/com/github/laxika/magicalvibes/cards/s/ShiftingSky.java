package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllNonlandPermanentsAreChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;

@CardRegistration(set = "8ED", collectorNumber = "100")
public class ShiftingSky extends Card {

    public ShiftingSky() {
        // As this enchantment enters, choose a color.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        // All nonland permanents are the chosen color.
        addEffect(EffectSlot.STATIC, new AllNonlandPermanentsAreChosenColorEffect());
    }
}
