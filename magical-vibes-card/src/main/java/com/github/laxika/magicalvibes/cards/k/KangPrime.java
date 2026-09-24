package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandAndSuspendEffect;

@CardRegistration(set = "MSC", collectorNumber = "85")
@CardRegistration(set = "MSC", collectorNumber = "406")
public class KangPrime extends Card {

    public KangPrime() {
        ExileTopUntilNonlandAndSuspendEffect effect = new ExileTopUntilNonlandAndSuspendEffect(2);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, effect);
        addEffect(EffectSlot.ON_ATTACK, effect);
    }
}
