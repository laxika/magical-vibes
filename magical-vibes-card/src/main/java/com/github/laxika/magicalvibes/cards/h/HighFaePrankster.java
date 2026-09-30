package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyExchangeTargetCreatureBasePowerEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetSourceBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "20")
public class HighFaePrankster extends Card {

    public HighFaePrankster() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Perpetually exchange target creature's base power with another target creature's base power.",
                                List.of(new PerpetuallyExchangeTargetCreatureBasePowerEffect()),
                                TargetFilters.creature(), null, 2, 2, false, null),
                        new ChooseOneEffect.ChooseOneOption(
                                "High Fae Prankster perpetually has base power and toughness 4/1.",
                                new PerpetuallySetSourceBasePowerToughnessEffect(4, 1)
                        )), true, 0, 1)));
    }
}
