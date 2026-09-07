package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "VOW", collectorNumber = "192")
public class CloakedCadet extends Card {

    public CloakedCadet() {
        addEffect(EffectSlot.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_HUMAN,
                new OncePerTurnTriggerEffect(new DrawCardEffect(1)));
    }
}
