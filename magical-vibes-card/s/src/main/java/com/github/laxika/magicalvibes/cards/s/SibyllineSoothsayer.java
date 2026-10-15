package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandWithManaValueAtLeastAndSuspendEffect;

@CardRegistration(set = "WHO", collectorNumber = "95")
@CardRegistration(set = "WHO", collectorNumber = "700")
public class SibyllineSoothsayer extends Card {

    public SibyllineSoothsayer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealUntilNonlandWithManaValueAtLeastAndSuspendEffect(3, 3));
    }
}
