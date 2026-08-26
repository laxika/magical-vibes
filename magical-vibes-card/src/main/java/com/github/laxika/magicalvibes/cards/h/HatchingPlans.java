package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "GPT", collectorNumber = "27")
public class HatchingPlans extends Card {

    public HatchingPlans() {
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, new DrawCardEffect(3));
    }
}
