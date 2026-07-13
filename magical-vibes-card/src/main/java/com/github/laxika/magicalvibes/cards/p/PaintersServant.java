package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsGainChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;

@CardRegistration(set = "SHM", collectorNumber = "257")
public class PaintersServant extends Card {

    public PaintersServant() {
        // As this creature enters, choose a color.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        // All permanents are the chosen color in addition to their other colors.
        addEffect(EffectSlot.STATIC, new AllPermanentsGainChosenColorEffect());
    }
}
