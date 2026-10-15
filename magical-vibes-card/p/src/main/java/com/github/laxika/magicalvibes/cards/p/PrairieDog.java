package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "24")
public class PrairieDog extends Card {

    public PrairieDog() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new NotCondition(new ControllerCastSpellThisTurn(new CardTruePredicate(), true)),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new GrantEffectToSourceUntilEndOfTurnEffect(
                        EffectSlot.STATIC, new AddOnePlusOneCountersEffect())),
                "{4}{W}: If you would put one or more +1/+1 counters on a creature you control, put "
                        + "that many plus one +1/+1 counters on it instead."
        ));
    }
}
