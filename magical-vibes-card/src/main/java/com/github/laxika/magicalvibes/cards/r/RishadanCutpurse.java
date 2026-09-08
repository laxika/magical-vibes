package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentUnlessPaysEffect;

@CardRegistration(set = "MMQ", collectorNumber = "93")
public class RishadanCutpurse extends Card {

    public RishadanCutpurse() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachOpponentSacrificesPermanentUnlessPaysEffect("{1}"));
    }
}
