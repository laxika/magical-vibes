package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentUnlessPaysEffect;

@CardRegistration(set = "MMQ", collectorNumber = "94")
public class RishadanFootpad extends Card {

    public RishadanFootpad() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachOpponentSacrificesPermanentUnlessPaysEffect("{2}"));
    }
}
