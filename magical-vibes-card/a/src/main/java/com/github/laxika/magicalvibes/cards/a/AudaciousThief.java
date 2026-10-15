package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "M20", collectorNumber = "84")
public class AudaciousThief extends Card {

    public AudaciousThief() {
        addEffect(EffectSlot.ON_ATTACK,
                SequenceEffect.of(new DrawCardEffect(), new LoseLifeEffect(1)));
    }
}
