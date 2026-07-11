package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "11")
public class LiuBeiLordOfShu extends Card {

    public LiuBeiLordOfShu() {
        // Liu Bei gets +2/+2 as long as you control a permanent named Guan Yu, Sainted Warrior
        // or a permanent named Zhang Fei, Fierce Warrior. (Horsemanship comes from metadata.)
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentAnyOfPredicate(List.of(
                        new PermanentNamedPredicate("Guan Yu, Sainted Warrior"),
                        new PermanentNamedPredicate("Zhang Fei, Fierce Warrior")))),
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
