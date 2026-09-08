package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "171")
public class FiremindsResearch extends Card {

    public FiremindsResearch() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                List.of(new PutCountersOnSelfEffect(CounterType.CHARGE))
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.CHARGE),
                        new DrawCardEffect(1)
                ),
                "{1}{U}, Remove two charge counters from Firemind's Research: Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new RemoveCounterFromSourceCost(5, CounterType.CHARGE),
                        new DealDamageToAnyTargetEffect(5)
                ),
                "{1}{R}, Remove five charge counters from Firemind's Research: It deals 5 damage to any target."
        ));
    }
}
