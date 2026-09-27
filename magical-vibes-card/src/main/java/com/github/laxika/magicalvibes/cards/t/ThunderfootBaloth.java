package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "C14", collectorNumber = "49")
public class ThunderfootBaloth extends Card {

    public ThunderfootBaloth() {
        ControlledCommanderAsCast lieutenant = new ControlledCommanderAsCast();

        addEffect(EffectSlot.STATIC, new ConditionalEffect(lieutenant,
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(lieutenant,
                new StaticBoostEffect(2, 2, Set.of(Keyword.TRAMPLE), GrantScope.OWN_CREATURES)));
    }
}
