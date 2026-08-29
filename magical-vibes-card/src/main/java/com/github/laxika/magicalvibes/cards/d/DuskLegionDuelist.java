package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "MOM", collectorNumber = "11")
public class DuskLegionDuelist extends Card {

    public DuskLegionDuelist() {
        addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
                new OncePerTurnTriggerEffect(new DrawCardEffect()));
    }
}
