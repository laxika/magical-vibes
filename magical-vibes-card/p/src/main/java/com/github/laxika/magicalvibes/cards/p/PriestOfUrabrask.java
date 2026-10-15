package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "NPH", collectorNumber = "90")
public class PriestOfUrabrask extends Card {

    public PriestOfUrabrask() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AwardManaEffect(ManaColor.RED, 3));
    }
}
