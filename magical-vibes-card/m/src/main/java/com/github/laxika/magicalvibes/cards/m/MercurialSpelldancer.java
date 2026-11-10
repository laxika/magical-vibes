package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "61")
public class MercurialSpelldancer extends Card {

    public MercurialSpelldancer() {
        addStaticEffect(new CantBeBlockedEffect());

        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new PutCountersOnSelfEffect(CounterType.OIL));

        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                new ConditionalEffect(
                        new AllOf(List.of(new SourceIsOnBattlefield(),
                                new SourceCounterThreshold(2, CounterType.OIL))),
                        SequenceEffect.of(
                                new RemoveCounterFromSourceEffect(CounterType.OIL, 2),
                                new CopyNextInstantOrSorceryCastThisTurnEffect())),
                "Remove two oil counters from Mercurial Spelldancer?"));
    }
}
