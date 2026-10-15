package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsUnlockedRoomDoorsCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "DSK", collectorNumber = "151")
public class RampagingSoulrager extends Card {

    public RampagingSoulrager() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsUnlockedRoomDoorsCount(2),
                new StaticBoostEffect(3, 0, GrantScope.SELF)));
    }
}
