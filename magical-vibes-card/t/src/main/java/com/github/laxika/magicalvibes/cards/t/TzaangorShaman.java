package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;

@CardRegistration(set = "40K", collectorNumber = "146")
public class TzaangorShaman extends Card {

    public TzaangorShaman() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CopyNextInstantOrSorceryCastThisTurnEffect());
    }
}
