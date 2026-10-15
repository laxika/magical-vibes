package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "23a")
@CardRegistration(set = "FEM", collectorNumber = "23b")
@CardRegistration(set = "FEM", collectorNumber = "23c")
public class Merseine extends Card {

    public Merseine() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new EnterWithCountersEffect(CounterType.NET, new Fixed(3)))
                .addEffect(EffectSlot.STATIC,
                        DoesntUntapWithCounterEffect.enchanted(CounterType.NET));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new RemoveCounterFromSourceCost(1, CounterType.NET)),
                "Pay enchanted creature's mana cost: Remove a net counter from this Aura. Only the controller of the enchanted creature may activate this ability.")
                .withActivatableByAnyPlayer()
                .withActivatableOnlyByEnchantedPermanentController()
                .withManaCostOfEnchantedPermanent());
    }
}
