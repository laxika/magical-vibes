package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "292")
public class StandDeliver extends Card {

    public StandDeliver() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Stand — Prevent the next 2 damage that would be dealt to target creature this turn",
                        PreventDamageEffect.nextToTargetCreature(2),
                        TargetFilters.creature()).withManaCost("{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Deliver — Return target permanent to its owner's hand",
                        ReturnToHandEffect.target(),
                        TargetFilters.permanent()).withManaCost("{2}{U}")
        )));
    }
}
