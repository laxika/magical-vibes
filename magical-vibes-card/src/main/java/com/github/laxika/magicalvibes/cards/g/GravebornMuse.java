package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;

public class GravebornMuse extends Card {

    public GravebornMuse() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DrawAndLoseLifePerSubtypeEffect(CardSubtype.ZOMBIE));
    }
}
