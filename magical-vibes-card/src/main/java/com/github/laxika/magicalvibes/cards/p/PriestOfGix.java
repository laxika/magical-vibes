package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "USG", collectorNumber = "150")
public class PriestOfGix extends Card {

    public PriestOfGix() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AwardManaEffect(ManaColor.BLACK, 3));
    }
}
