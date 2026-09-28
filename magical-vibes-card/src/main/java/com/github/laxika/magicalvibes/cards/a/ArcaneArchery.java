package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCounterToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "65")
public class ArcaneArchery extends Card {

    public ArcaneArchery() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3))
                .addEffect(EffectSlot.SPELL,
                        new GrantKeywordEffect(Set.of(Keyword.REACH, Keyword.TRAMPLE), GrantScope.TARGET));

        addEffect(EffectSlot.SPELL,
                RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                        new CardTypePredicate(CardType.CREATURE),
                        List.of(
                                new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1),
                                new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                        CounterType.REACH, 1),
                                new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                        CounterType.TRAMPLE, 1))));
    }
}
