package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "9ED", collectorNumber = "152")
public class PhyrexianArena extends Card {

    public PhyrexianArena() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DrawCardEffect(1));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(1));
    }
}
