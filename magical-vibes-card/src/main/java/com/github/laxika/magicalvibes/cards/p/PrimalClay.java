package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChoosePrimalClayFormOnEnterEffect;

@CardRegistration(set = "6ED", collectorNumber = "308")
@CardRegistration(set = "5ED", collectorNumber = "395")
@CardRegistration(set = "4ED", collectorNumber = "342")
public class PrimalClay extends Card {

    public PrimalClay() {
        // As this creature enters, it becomes your choice of a 3/3 artifact creature, a 2/2 artifact
        // creature with flying, or a 1/6 Wall artifact creature with defender. The chosen shape's
        // base P/T, keyword, and creature type are stamped onto the permanent when the choice resolves.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChoosePrimalClayFormOnEnterEffect());
    }
}
