package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachCreatureControllerSacrificesPermanentUnlessPaysEffect;

@CardRegistration(set = "EXO", collectorNumber = "34")
public class FadeAway extends Card {

    public FadeAway() {
        addEffect(EffectSlot.SPELL,
                new EachCreatureControllerSacrificesPermanentUnlessPaysEffect("{1}"));
    }
}
