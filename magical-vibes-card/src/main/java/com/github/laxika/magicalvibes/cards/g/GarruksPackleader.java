package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMinPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "177")
public class GarruksPackleader extends Card {

    public GarruksPackleader() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3,
                        new MayEffect(new DrawCardEffect(1), "Draw a card?")));
    }
}
