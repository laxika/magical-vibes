package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "RIX", collectorNumber = "132")
public class HardyVeteran extends Card {

    public HardyVeteran() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new StaticBoostEffect(0, 2, GrantScope.SELF)));
    }
}
