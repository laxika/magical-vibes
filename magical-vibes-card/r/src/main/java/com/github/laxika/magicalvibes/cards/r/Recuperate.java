package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "21")
public class Recuperate extends Card {

    public Recuperate() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "You gain 6 life",
                        new GainLifeEffect(6)),
                new ChooseOneEffect.ChooseOneOption(
                        "Prevent the next 6 damage that would be dealt to target creature this turn",
                        PreventDamageEffect.nextToTargetCreature(6),
                        TargetFilters.creature())
        )));
    }
}
