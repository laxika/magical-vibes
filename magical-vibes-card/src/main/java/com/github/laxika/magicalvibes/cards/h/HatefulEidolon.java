package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachAuraAttachedToDyingCreatureEffect;

@CardRegistration(set = "THB", collectorNumber = "101")
public class HatefulEidolon extends Card {

    public HatefulEidolon() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new DrawCardForEachAuraAttachedToDyingCreatureEffect());
    }
}
