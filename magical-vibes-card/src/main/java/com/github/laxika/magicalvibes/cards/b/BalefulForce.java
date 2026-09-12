package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "VMA", collectorNumber = "105")
public class BalefulForce extends Card {

    public BalefulForce() {
        // At the beginning of each upkeep, you draw a card and you lose 1 life.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
    }
}
