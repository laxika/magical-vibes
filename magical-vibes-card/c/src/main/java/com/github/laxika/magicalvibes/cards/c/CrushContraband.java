package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "7")
@CardRegistration(set = "PIP", collectorNumber = "158")
@CardRegistration(set = "PIP", collectorNumber = "686")
@CardRegistration(set = "NEC", collectorNumber = "82")
@CardRegistration(set = "VOC", collectorNumber = "81")
@CardRegistration(set = "SCD", collectorNumber = "16")
@CardRegistration(set = "ZNC", collectorNumber = "13")
public class CrushContraband extends Card {

    public CrushContraband() {
        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target artifact",
                        new ExileTargetPermanentEffect(),
                        TargetFilters.artifact()),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target enchantment",
                        new ExileTargetPermanentEffect(),
                        TargetFilters.enchantment())
        )));
    }
}
