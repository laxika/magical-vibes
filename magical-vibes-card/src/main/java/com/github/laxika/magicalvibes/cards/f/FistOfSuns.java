package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;

@CardRegistration(set = "5DN", collectorNumber = "123")
public class FistOfSuns extends Card {

    public FistOfSuns() {
        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect("{W}{U}{B}{R}{G}", null));
    }
}
