package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "9")
public class Infestation extends Card {

    public Infestation() {
        // Evoke {1}{B}{B}, pay 3 life; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{1}{B}{B}"), new LifeCastingCost(3))));

        // When this creature enters, conjure a card named Blowfly Infestation onto the battlefield.
        // Then put a -1/-1 counter on each creature.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new ConjureCardToBattlefieldEffect("Blowfly Infestation"),
                new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.MINUS_ONE_MINUS_ONE, 1,
                        new PermanentIsCreaturePredicate(), EachPermanentScope.ALL_PLAYERS)));

        // Evoke sacrifice: if it was cast for its evoke cost, sacrifice it as it enters.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeSelfIfEvokedEffect());
    }
}
