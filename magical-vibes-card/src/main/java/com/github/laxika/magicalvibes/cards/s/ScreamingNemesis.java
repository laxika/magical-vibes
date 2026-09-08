package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect;

@CardRegistration(set = "DSK", collectorNumber = "157")
public class ScreamingNemesis extends Card {

    public ScreamingNemesis() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect(new EventValue()));
    }
}
