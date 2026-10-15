package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileIfLeavesBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "96")
public class PersonalDecoy extends Card {

    public PersonalDecoy() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.LOYALTY, new ControllerLifeTotal()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileIfLeavesBattlefieldEffect());
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackControllerUnlessPredicateEffect(
                new PermanentNotPredicate(new PermanentTruePredicate())));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new GainLifeEffect(1)),
                "+1: You gain 1 life."
        ));
        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new DrawCardEffect()),
                "\u22124: Draw a card."
        ));
    }
}
