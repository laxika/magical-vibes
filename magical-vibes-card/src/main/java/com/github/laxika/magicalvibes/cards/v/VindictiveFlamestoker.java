package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "154")
public class VindictiveFlamestoker extends Card {

    public VindictiveFlamestoker() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new PutCountersOnSelfEffect(CounterType.OIL))
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{R}",
                List.of(
                        new ReduceActivationCostEffect(new CountersOnSource(CounterType.OIL)),
                        new SacrificeSelfCost(),
                        new DiscardHandCost(),
                        new DrawCardEffect(4)
                ),
                "{6}{R}, Sacrifice this creature: Discard your hand, then draw four cards. This ability costs {1} less to activate for each oil counter on this creature."
        ));
    }
}
