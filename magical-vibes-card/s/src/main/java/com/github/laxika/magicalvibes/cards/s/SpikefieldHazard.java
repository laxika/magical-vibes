package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenExileIfDamagedEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "166")
public class SpikefieldHazard extends Card {

    public SpikefieldHazard() {
        setBackFaceCard(new SpikefieldCave());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Spikefield Hazard",
                        new DealDamageToAnyTargetThenExileIfDamagedEffect(new Fixed(1))),
                new ChooseOneEffect.ChooseOneOption("Spikefield Cave", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "SpikefieldCave";
    }
}
