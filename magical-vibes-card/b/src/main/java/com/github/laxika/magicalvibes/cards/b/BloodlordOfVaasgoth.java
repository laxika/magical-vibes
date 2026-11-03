package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantBloodthirstToCastSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Bloodthirst 3 (CR 702.54a) is a static ability that reads "If an opponent was dealt damage this
 * turn, this permanent enters with three +1/+1 counters on it", so it is an as-enters replacement
 * rather than an ETB trigger. The Vampire trigger hands the same static ability to a creature spell
 * while it is still on the stack; each instance applies separately (CR 702.54c).
 */
@CardRegistration(set = "M12", collectorNumber = "82")
@CardRegistration(set = "VOC", collectorNumber = "121")
@CardRegistration(set = "C17", collectorNumber = "102")
public class BloodlordOfVaasgoth extends Card {

    public BloodlordOfVaasgoth() {
        addStaticEffect(new ConditionalEffect(new OpponentDealtDamageThisTurn(1),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3))));
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.VAMPIRE)))),
                new GrantBloodthirstToCastSpellEffect(3));
    }
}
