package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.PermanentBasePowerEqualsPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "6")
public class PrimoTheUnbounded extends Card {

    public PrimoTheUnbounded() {
        // Primo enters with twice X +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new Scaled(new XValue(), 2)));

        // Whenever one or more creatures you control with base power 0 deal combat damage to a
        // player, create a Fractal token and put counters on it equal to the damage dealt.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentBasePowerEqualsPredicate(0),
                        new CreateXTokenWithXCountersEffect(
                                "Fractal", 0, 0,
                                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                                List.of(CardSubtype.FRACTAL),
                                CounterType.PLUS_ONE_PLUS_ONE, new EventValue()),
                        false,
                        true));
    }
}
