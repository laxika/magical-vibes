package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "VOW", collectorNumber = "223")
public class SporebackWolf extends Card {

    public SporebackWolf() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new StaticBoostEffect(0, 2, GrantScope.SELF)));
    }
}
