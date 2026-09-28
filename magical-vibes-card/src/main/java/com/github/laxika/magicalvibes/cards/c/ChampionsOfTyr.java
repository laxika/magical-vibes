package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCounterToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "21")
public class ChampionsOfTyr extends Card {

    public ChampionsOfTyr() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                        new CardTypePredicate(CardType.CREATURE),
                        List.of(new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put a +1/+1 counter on that creature",
                                        new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                                CounterType.PLUS_ONE_PLUS_ONE, 1)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put a flying counter on that creature",
                                        new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                                CounterType.FLYING, 1)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put a lifelink counter on that creature",
                                        new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                                CounterType.LIFELINK, 1)))))));
    }
}
