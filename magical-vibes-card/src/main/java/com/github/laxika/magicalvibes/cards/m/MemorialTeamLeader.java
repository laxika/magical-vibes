package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "144")
public class MemorialTeamLeader extends Card {

    public MemorialTeamLeader() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES)));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{R}"))));
    }
}
