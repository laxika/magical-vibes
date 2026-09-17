package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantStormToNextInstantOrSorceryCastThisTurnEffect;

@CardRegistration(set = "MAR", collectorNumber = "94")
public class StormForceOfNature extends Card {

    public StormForceOfNature() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new GrantStormToNextInstantOrSorceryCastThisTurnEffect());
    }
}
