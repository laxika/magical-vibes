package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "74")
public class TwistedReflection extends Card {

    public TwistedReflection() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{B}"));
        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -6/-0 until end of turn",
                        new BoostTargetCreatureEffect(-6, 0),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Switch target creature's power and toughness until end of turn",
                        new SwitchPowerToughnessEffect(),
                        TargetFilters.creature())
        )));
    }
}
