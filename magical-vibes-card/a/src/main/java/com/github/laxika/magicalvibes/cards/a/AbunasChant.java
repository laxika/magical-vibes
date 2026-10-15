package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "1")
public class AbunasChant extends Card {

    public AbunasChant() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "You gain 5 life",
                        new GainLifeEffect(5)),
                new ChooseOneEffect.ChooseOneOption(
                        "Prevent the next 5 damage that would be dealt to target creature this turn",
                        PreventDamageEffect.nextToTargetCreature(5),
                        TargetFilters.creature())
        )));
    }
}
