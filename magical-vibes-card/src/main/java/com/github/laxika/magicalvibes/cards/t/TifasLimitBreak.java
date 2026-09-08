package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TieredManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "207")
public class TifasLimitBreak extends Card {

    public TifasLimitBreak() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Somersault - {0} - Target creature gets +2/+2 until end of turn",
                        new BoostTargetCreatureEffect(2, 2), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Meteor Strikes - {2} - Double target creature's power and toughness until end of turn",
                        new BoostTargetCreatureEffect(new TargetPower(), new TargetToughness()),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Final Heaven - {6}{G} - Triple target creature's power and toughness until end of turn",
                        new BoostTargetCreatureEffect(
                                new Scaled(new TargetPower(), 2),
                                new Scaled(new TargetToughness(), 2)),
                        TargetFilters.creature())
        )));
        addEffect(EffectSlot.SPELL, new TieredManaCost(List.of("", "{2}", "{6}{G}")));
    }
}
