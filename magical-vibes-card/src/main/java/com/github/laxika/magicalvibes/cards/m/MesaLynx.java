package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "ZNR", collectorNumber = "28")
public class MesaLynx extends Card {

    public MesaLynx() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new NotControllerTurn(),
                new StaticBoostEffect(0, 2, GrantScope.SELF)));
    }
}
