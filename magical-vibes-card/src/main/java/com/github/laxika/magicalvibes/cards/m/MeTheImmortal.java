package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PreserveCountersOnZoneChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "147")
public class MeTheImmortal extends Card {

    public MeTheImmortal() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "+1/+1 counter", new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                new ChooseOneEffect.ChooseOneOption(
                        "first strike counter", new PutCountersOnSelfEffect(CounterType.FIRST_STRIKE)),
                new ChooseOneEffect.ChooseOneOption(
                        "vigilance counter", new PutCountersOnSelfEffect(CounterType.VIGILANCE)),
                new ChooseOneEffect.ChooseOneOption(
                        "menace counter", new PutCountersOnSelfEffect(CounterType.MENACE))
        )));
        addEffect(EffectSlot.STATIC, new PreserveCountersOnZoneChangeEffect());
        addCastingOption(new GraveyardCast(List.of(new DiscardCardCastingCost(null, null, 2))));
    }
}
