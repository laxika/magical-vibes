package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MID", collectorNumber = "117")
public class NoviceOccultist extends Card {

    public NoviceOccultist() {
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new DrawCardEffect(1),
                new LoseLifeEffect(1)));
    }
}
