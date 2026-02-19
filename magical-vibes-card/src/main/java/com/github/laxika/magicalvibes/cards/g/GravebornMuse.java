package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "145")
public class GravebornMuse extends Card {

    public GravebornMuse() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DrawAndLoseLifePerSubtypeEffect(CardSubtype.ZOMBIE));
    }
}
